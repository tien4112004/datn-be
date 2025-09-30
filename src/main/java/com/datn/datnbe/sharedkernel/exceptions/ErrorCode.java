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

    UNSUPPORTED_MEDIA_TYPE("Unsupported media type", 415), FILE_TOO_LARGE("File size exceeds the limit", 413),
    FILE_UPLOAD_ERROR("File upload failed", 500), MEDIA_NOT_FOUND("Media not found", 404),

    IDEMPOTENCY_KEY_MISSING("Idempotency key is missing", 400),
    IDEMPOTENCY_KEY_INVALID("Idempotency key is invalid", 400),

    AI_RESULT_NOT_FOUND("AI Result not found", 404),

    AI_WORKER_UNPROCESSABLE_ENTITY("AI Worker cannot process the request", 422),
    AI_WORKER_SERVER_ERROR("AI Worker encountered an error", 500),
    AI_WORKER_UNAVAILABLE("AI Worker is unavailable", 503),

    FILE_PROCESSING_ERROR("Error processing the file", 500), INVALID_BASE64_FORMAT("Invalid Base64 format", 500),

    IMAGE_INSERTION_FAILED("Failed to insert image into presentation", 400);

    private final String defaultMessage;
    private final Integer statusCode;

    public String getErrorCodeName() {
        return this.name();
    }
}
