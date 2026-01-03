package com.alfarays.user.service;

import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.user.model.ChangePasswordRequest;
import com.alfarays.user.model.UserFilterDTO;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.model.UserUpdateRequest;
import com.alfarays.util.GlobalResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {

    GlobalResponse<UserResponse> byEmail(String email);

    GlobalResponse<UserResponse> byId(Long id);

    GlobalResponse<UserResponse> update(UserUpdateRequest request, String authenticator);

    GlobalResponse<Boolean> delete(String userId) throws IOException;

    GlobalResponse<List<UserResponse>> filterUsers(UserFilterDTO filter, Pageable pageable);

    GlobalResponse<String> changePassword(ChangePasswordRequest request, String authenticator);

    GlobalResponse<String> changeProfilePicture(MultipartFile profile, String authenticator);
}
