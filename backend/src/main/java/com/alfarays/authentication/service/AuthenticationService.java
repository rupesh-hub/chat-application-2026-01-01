package com.alfarays.authentication.service;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.AuthenticationResponse;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.exceptions.InvalidCredentialsException;
import com.alfarays.exceptions.UserAccountException;
import com.alfarays.image.entity.Image;
import com.alfarays.image.service.ImageService;
import com.alfarays.security.JwtTokenService;
import com.alfarays.user.mapper.UserMapper;
import com.alfarays.user.model.PrincipleUser;
import com.alfarays.user.repository.UserRepository;
import com.alfarays.util.GlobalResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final ImageService imageService;

    @Override
    @Transactional
    public GlobalResponse<String> register(RegistrationRequest request, MultipartFile profile) throws MethodArgumentNotValidException, IOException {

        // 1. Check if email exists
        var optionalUserByEmail = userRepository.findByEmail(request.email());
        if(optionalUserByEmail.isPresent()) throwEmailError(request.email());

        // 2. Map and persist the user first to get an ID
        var user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        // Save user first
        var savedUser = userRepository.save(user);

        // 3. Handle Profile Upload
        if(profile != null && !profile.isEmpty()) {
            // Upload the image
            Image imageEntity = imageService.upload(profile, savedUser);

            // 🔥 Set both sides of the relationship
            imageEntity.setUser(savedUser);
            savedUser.setProfile(imageEntity);

            // 4. Save again to update the User with the Profile link
            userRepository.save(savedUser);
        }

        return GlobalResponse.success(savedUser.getEmail());
    }

    @Override
    public GlobalResponse<Boolean> existsByEmail(String email) {
        return userRepository.emailExists(email)
                .map(GlobalResponse::success)
                .orElse(GlobalResponse.success(Boolean.FALSE));
    }

    @Override
    public GlobalResponse<AuthenticationResponse> login(AuthenticationRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            PrincipleUser principleUser = (PrincipleUser) authentication.getPrincipal();
            var user = principleUser.user();

            String name = user.getFirstname() + " " + user.getLastname();
            String role = principleUser.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("USER"); // Use orElse to avoid .get() NoSuchElementException

            // Put only the path string.
            Map<String, Object> claims = new HashMap<>();
            claims.put("name", name);
            claims.put("email", user.getEmail());
            claims.put("role", role);

            // Only put the string path in the JWT, not the whole Image object
            if(user.getProfile() != null) {
                claims.put("profile", user.getProfile().getPath());
            }

            var token = tokenService.generateToken(principleUser, claims);

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .name(name)
                    .email(user.getEmail())
                    .profile(user.getProfile() != null ? user.getProfile().getPath() : null)
                    .token(token)
                    .role(role)
                    .build();

            return GlobalResponse.success(response);

        } catch(BadCredentialsException e) {
            throw new InvalidCredentialsException("Bad Credentials!");
        } catch(DisabledException e) {
            throw new UserAccountException("Your account is disabled", "ACCOUNT_DISABLED");
        } catch(LockedException e) {
            throw new UserAccountException("Your account is locked", "ACCOUNT_LOCKED");
        } catch(Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getLocalizedMessage());
        }
    }

    private void throwEmailError(String email) throws MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");
        bindingResult.addError(new FieldError("user", "email", String.format("User with email '%s' already exists.", email)));
        // Find the register method reference for the exception constructor
        Method method = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals("register"))
                .findFirst().orElseThrow();

        throw new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);
    }

}
