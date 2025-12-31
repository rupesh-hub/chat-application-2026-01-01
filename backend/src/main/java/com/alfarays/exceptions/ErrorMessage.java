package com.alfarays.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private String code;
    private String message;
    private HttpStatus status;
    private String uri;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

}