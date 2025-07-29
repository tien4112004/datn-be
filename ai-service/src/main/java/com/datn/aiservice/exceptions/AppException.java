package com.datn.aiservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AppException extends RuntimeException {
    ErrorCode errorCode;
    Integer statusCode;
    String errorMessage;

    public AppException(ErrorCode errorCode) {
        this(errorCode, errorCode.getStatusCode(), errorCode.getDefaultMessage());
    }
}
