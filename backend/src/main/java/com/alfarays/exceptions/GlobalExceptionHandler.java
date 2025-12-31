package com.alfarays.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorMessage> handleLockedException(LockedException exp, HttpServletRequest request) {
        log.warn("[v0] Account locked attempt: {}", request.getRemoteUser());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message("Your account is locked due to multiple failed login attempts. Please contact support.")
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorMessage> handleDisabledException(DisabledException exp, HttpServletRequest request) {
        log.warn("[v0] Disabled account access attempt: {}", request.getRemoteUser());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message("Your account is disabled. Please contact administrator.")
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleBadCredentialsException(BadCredentialsException exp, HttpServletRequest request) {
        log.warn("[v0] Bad credentials attempt for: {}", request.getParameter("username"));
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message("Invalid username or password.")
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorMessage> handleAuthorizationException(AuthorizationException exp, HttpServletRequest request) {
        log.warn("[v0] Authorization exception: {}", exp.getMessage());
        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("403")
                                .message(exp.getMessage())
                                .status(FORBIDDEN)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(AccessDeniedException exp, HttpServletRequest request) {
        log.warn("[v0] Access denied: {}", exp.getMessage());
        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("403")
                                .message(exp.getMessage())
                                .status(FORBIDDEN)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorMessage> handleTokenExpiredException(TokenExpiredException exp, HttpServletRequest request) {
        log.warn("[v0] Token expired: {}", request.getRequestURI());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message(exp.getMessage())
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleInvalidCredentialsException(InvalidCredentialsException exp, HttpServletRequest request) {
        log.warn("[v0] Invalid credentials: {}", exp.getMessage());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message(exp.getMessage())
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException(ResourceNotFoundException exp, HttpServletRequest request) {
        log.warn("[v0] Resource not found: {}", exp.getMessage());
        return ResponseEntity
                .status(NOT_FOUND)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("404")
                                .message(exp.getMessage())
                                .status(NOT_FOUND)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(UserAccountException.class)
    public ResponseEntity<ErrorMessage> handleUserAccountException(UserAccountException exp, HttpServletRequest request) {
        log.warn("[v0] User account exception: {}", exp.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code(exp.getCode())
                                .message(exp.getMessage())
                                .status(BAD_REQUEST)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorMessage> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exp, HttpServletRequest request) {
        log.warn("[v0] File upload size exceeded");
        return ResponseEntity
                .status(PAYLOAD_TOO_LARGE)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("413")
                                .message("File size exceeds the maximum limit of 10MB.")
                                .status(PAYLOAD_TOO_LARGE)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorMessage> handleNoHandlerFoundException(NoHandlerFoundException exp, HttpServletRequest request) {
        log.warn("[v0] Endpoint not found: {} {}", exp.getHttpMethod(), exp.getRequestURL());
        return ResponseEntity
                .status(NOT_FOUND)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("404")
                                .message("Endpoint not found.")
                                .status(NOT_FOUND)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exp, HttpServletRequest servletRequest) {

        Map<String, String> errors = new HashMap<>();

        exp.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    if(error instanceof FieldError fieldError)
                        errors.put(fieldError.getField(), error.getDefaultMessage());
                    else
                        errors.put(error.getObjectName(), error.getDefaultMessage());
                });

        log.warn("[v0] Validation failed for: {}", servletRequest.getRequestURI());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("400")
                                .message("Validation failed!")
                                .status(BAD_REQUEST)
                                .errors(errors)
                                .uri(servletRequest.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException exp, HttpServletRequest request) {
        log.warn("[v0] Authentication failed: {}", exp.getMessage());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("401")
                                .message("Authentication failed. Please log in with valid credentials.")
                                .status(UNAUTHORIZED)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorMessage> handleIOException(IOException exp, HttpServletRequest request) {
        log.error("[v0] IO Exception: {}", exp.getMessage());
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ErrorMessage.builder()
                                .timestamp(LocalDateTime.now())
                                .code("500")
                                .message("An error occurred while processing the file.")
                                .status(INTERNAL_SERVER_ERROR)
                                .uri(request.getRequestURI())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception exp, HttpServletRequest request) {
        log.error("[v0] Unexpected exception: {}", exp.getMessage(), exp);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ErrorMessage.builder()
                                .message("An unexpected error occurred. Please try again later.")
                                .code("500")
                                .timestamp(LocalDateTime.now())
                                .status(INTERNAL_SERVER_ERROR)
                                .uri(request.getRequestURI())
                                .build()
                );
    }
}
