package com.alfarays.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class AuthorizationException extends RuntimeException  {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String errorCode;

    public AuthorizationException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_SERVER_ERROR";
    }

    public AuthorizationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = status.name();
    }

    public AuthorizationException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_SERVER_ERROR";
    }

    public AuthorizationException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

}
