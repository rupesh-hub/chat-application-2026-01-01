package com.alfarays.user.service;

import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.image.service.ImageService;
import com.alfarays.user.entity.User;
import com.alfarays.user.mapper.UserMapper;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
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
    public GlobalResponse<Boolean> update(RegistrationRequest request, String email, MultipartFile profile) throws IOException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists."));
        if(StringUtils.isNotBlank(request.lastname()) || StringUtils.isNotEmpty(request.lastname()))
            user.setLastname(request.lastname());
        if(StringUtils.isNotBlank(request.firstname()) || StringUtils.isNotEmpty(request.firstname()))
            user.setFirstname(request.firstname());
        userRepository.save(user);
        return GlobalResponse.success(Boolean.TRUE);
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


}
