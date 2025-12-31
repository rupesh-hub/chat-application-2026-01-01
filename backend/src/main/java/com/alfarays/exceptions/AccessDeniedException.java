package com.alfarays.exceptions;


import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException {
    private final String code = "ACCESS_DENIED";

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
