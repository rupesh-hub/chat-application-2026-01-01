package com.alfarays.user.service;

import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.image.entity.Image;
import com.alfarays.image.repository.ImageRepository;
import com.alfarays.image.service.ImageService;
import com.alfarays.user.entity.User;
import com.alfarays.user.mapper.UserMapper;
import com.alfarays.user.model.ChangePasswordRequest;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.model.UserUpdateRequest;
import com.alfarays.user.repository.UserRepository;
import com.alfarays.user.repository.UserSpecification;
import com.alfarays.util.GlobalResponse;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;

    @Override
    public GlobalResponse<UserResponse> byEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse)
                .map(GlobalResponse::success)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
    }

    @Override
    public GlobalResponse<UserResponse> byId(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .map(GlobalResponse::success)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
    }

    @Override
    public GlobalResponse<UserResponse> update(UserUpdateRequest request, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
        if(StringUtils.isNotBlank(request.lastname()) || StringUtils.isNotEmpty(request.lastname()))
            user.setLastname(request.lastname());
        if(StringUtils.isNotBlank(request.firstname()) || StringUtils.isNotEmpty(request.firstname()))
            user.setFirstname(request.firstname());
        user = userRepository.save(user);
        return GlobalResponse.success(UserMapper.toResponse(user));
    }

    @Override
    public GlobalResponse<Boolean> delete(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
        userRepository.delete(user);
        return GlobalResponse.success(Boolean.TRUE);
    }

    @Override
    public GlobalResponse<List<UserResponse>> filterUsers(UserFilterDTO filter, Pageable pageable) {

        Specification<User> spec = UserSpecification.withFilter(filter);

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdOn")
        );

        Page<User> userPage = userRepository.findAll(spec, pageRequest);

        return GlobalResponse.success(
                userPage.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList()
        );
    }

    @Override
    public GlobalResponse<String> changePassword(ChangePasswordRequest request, String authenticator) {
        var user = userRepository.findByEmail(authenticator)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists. "));
        if(!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new AuthorizationException("Current password is not correct !");
        if(!request.password().equals(request.confirmPassword()))
            throw new AuthorizationException("Password and new password do not match!");
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return GlobalResponse.success("Password change success !");
    }


    @Override
    public GlobalResponse<String> changeProfilePicture(MultipartFile profile, String authenticator) {
        var user = userRepository.findByEmail(authenticator)
                .orElseThrow(() -> new UsernameNotFoundException("User not exist."));
        Image image;
        try {
            image = imageService.update(profile, getProfile(user.getId()));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return GlobalResponse.success(image.getPath());
    }

    private Image getProfile(Long userId) {
        return imageRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthorizationException("Profile is not exists!"));
    }

}
