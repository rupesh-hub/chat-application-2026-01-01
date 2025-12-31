package com.alfarays.exceptions;


import lombok.Getter;

@Getter
public class UserAccountException extends RuntimeException {
    private final String code;

    public UserAccountException(String message, String code) {
        super(message);
        this.code = code;
    }
}
