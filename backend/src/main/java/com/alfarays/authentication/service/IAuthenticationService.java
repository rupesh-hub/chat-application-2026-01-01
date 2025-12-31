package com.alfarays.authentication.service;

import com.alfarays.authentication.model.AuthenticationRequest;
import com.alfarays.authentication.model.AuthenticationResponse;
import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.util.GlobalResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IAuthenticationService {
    GlobalResponse<String> register(RegistrationRequest request, MultipartFile profile) throws MethodArgumentNotValidException, IOException;

    GlobalResponse<Boolean> existsByEmail(String email);

    GlobalResponse<AuthenticationResponse> login(AuthenticationRequest request);
}
