package com.alfarays.exceptions;

import lombok.Getter;

@Getter
public class TokenExpiredException extends RuntimeException {
    private final String code = "TOKEN_EXPIRED";

    public TokenExpiredException(String message) {
        super(message);
    }
}
