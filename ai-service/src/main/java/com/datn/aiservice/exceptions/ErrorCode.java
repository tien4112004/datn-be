package com.datn.aiservice.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR("An unexpected error occurred", 500),

    MODEL_NOT_FOUND("Model not found", 404),
    MODEL_NOT_ENABLED("Model is not enabled for this operation", 403),
    ;

    private final String defaultMessage;
    private final Integer statusCode;

    public String getErrorCodeName() {
        return this.name();
    }
}
