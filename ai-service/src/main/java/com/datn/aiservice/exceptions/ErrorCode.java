package com.datn.aiservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR("An unexpected error occurred"),

    MODEL_NOT_FOUND("Model not found");

    private final String defaultMessage;

    public String getErrorCodeName() {
        return this.name();
    }
}
