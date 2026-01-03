package com.alfarays.user.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String name;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private String profile;
    private String status;
    private String lastSeen;
    private String createdAt;

}