package com.alfarays.exceptions;


import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {
    private final String code = "INVALID_CREDENTIALS";

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
