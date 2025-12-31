package com.alfarays.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {

    private String query;
    private String firstname;
    private String lastname;
    private String email;

}