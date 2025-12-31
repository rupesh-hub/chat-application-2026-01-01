package com.alfarays.user.mapper;


import com.alfarays.authentication.model.RegistrationRequest;
import com.alfarays.user.entity.User;
import com.alfarays.user.model.UserResponse;
import com.alfarays.util.Util;

import java.time.LocalDateTime;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(RegistrationRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setPassword(request.password());
        return user;
    }

    public static UserResponse toResponse(User user) {
        return UserResponse
                .builder()
                .id(user.getId())
                .name(name(user.getFirstname(), user.getLastname()))
                .email(user.getEmail())
                .profile(null != user.getProfile() ? user.getProfile().getPath() : null)
                .status("online")
                .lastSeen(LocalDateTime.now().toString())
                .build();
    }

    private static String name(String firstname, String lastname) {
        return Util.capitalize(firstname) + " " + Util.capitalize(lastname);
    }

}
