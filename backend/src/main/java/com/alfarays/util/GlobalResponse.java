package com.alfarays.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {

    String message;
    ResponseStatus status;
    String code;
    String error;
    List<String> errors;
    T data;
    Paging page;
    String timestamp;

    public enum ResponseStatus {
        SUCCESS, FAILURE
    }

    public static <T> GlobalResponse<T> success(T data) {
        return GlobalResponse.<T>builder()
                .message("Success")
                .status(ResponseStatus.SUCCESS)
                .code("200")
                .data(data)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static GlobalResponse<Void> failure(String errorMessage, String code) {
        return GlobalResponse.<Void>builder()
                .status(ResponseStatus.FAILURE)
                .code(code)
                .error(errorMessage)
                .timestamp(java.time.LocalDateTime.now().toString()) // Add this!
                .build();
    }
}