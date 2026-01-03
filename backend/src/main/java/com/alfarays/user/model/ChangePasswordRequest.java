package com.alfarays.user.model;

public record ChangePasswordRequest(
        String currentPassword,
        String password,
        String confirmPassword
) {
}
