package com.alfarays.user.model;

public record UserUpdateRequest(
        String firstname,
        String lastname
) {
}
