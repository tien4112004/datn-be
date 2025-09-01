package com.datn.datnbe.sharedkernel.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_ERROR("An unexpected error occurred", 500),

    MODEL_NOT_FOUND("Model not found", 404), MODEL_NOT_ENABLED("Model is not enabled for this operation", 403),
    INVALID_MODEL_STATUS("Invalid model status", 403),

    GENERATION_ERROR("Generation Failed", 500), JSON_PARSING_ERROR("Error parsing JSON response", 400),
    PRESENTATION_NOT_FOUND("Presentation not found", 404), SLIDE_NOT_FOUND("Slide not found", 404),
    PRESENTATION_TITLE_ALREADY_EXISTS("Presentation title already exists", 409),

    INVALID_PRESENTATION_DATA("Invalid presentation data", 400), INVALID_SLIDE_DATA("Invalid slide data", 400),
    INVALID_ELEMENT_DATA("Invalid element data", 400), MISSING_REQUIRED_FIELD("Missing required field", 400),
    INVALID_ELEMENT_TYPE("Invalid element type", 400), INVALID_BACKGROUND_TYPE("Invalid background type", 400),

    PRESENTATION_CREATION_FAILED("Failed to create presentation", 500),
    DATABASE_ERROR("Database operation failed", 500), VALIDATION_ERROR("Validation failed", 400),

    UNSUPPORTED_MEDIA_TYPE("Unsupported media type", 415),
    FILE_TOO_LARGE("File size exceeds the limit", 413),
    FILE_UPLOAD_ERROR("File upload failed", 500)
    ;

    private final String defaultMessage;
    private final Integer statusCode;

    public String getErrorCodeName() {
        return this.name();
    }
}
