package com.alfarays.authentication.model;

public record ResetPasswordRequest(
        String email,
        String password,
        String confirmPassword,
        String token
) {
}
