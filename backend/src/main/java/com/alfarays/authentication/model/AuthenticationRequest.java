package com.alfarays.authentication.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotBlank(message = "Email is mandatory.")
        @Email(message = "Enter a valid email")
        String email,

        @NotBlank(message = "Password is mandatory.")
        @Size(min = 8, message = "Password must be at least 8 characters long.")
        String password
) {
}
