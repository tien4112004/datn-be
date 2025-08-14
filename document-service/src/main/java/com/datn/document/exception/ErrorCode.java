package com.datn.document.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR("An unexpected error occurred", 500),

    PRESENTATION_NOT_FOUND("Presentation not found", 404), SLIDE_NOT_FOUND("Slide not found", 404),
    PRESENTATION_TITLE_ALREADY_EXISTS("Presentation title already exists", 409),

    INVALID_PRESENTATION_DATA("Invalid presentation data", 400), INVALID_SLIDE_DATA("Invalid slide data", 400),
    INVALID_ELEMENT_DATA("Invalid element data", 400), MISSING_REQUIRED_FIELD("Missing required field", 400),
    INVALID_ELEMENT_TYPE("Invalid element type", 400), INVALID_BACKGROUND_TYPE("Invalid background type", 400),

    PRESENTATION_CREATION_FAILED("Failed to create presentation", 500),
    DATABASE_ERROR("Database operation failed", 500), VALIDATION_ERROR("Validation failed", 400),;

    private final String defaultMessage;
    private final Integer statusCode;

    public String getErrorCodeName() {
        return this.name();
    }
}