package com.alfarays.authentication.model;

public record RegistrationRequest(
        String firstname,
        String lastname,
        String email,
        String password,
        String confirmPassword
) {
}
