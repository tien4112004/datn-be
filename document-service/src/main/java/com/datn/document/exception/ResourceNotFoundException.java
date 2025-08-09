package com.datn.document.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException() {
        super(ErrorCode.PRESENTATION_NOT_FOUND);
    }

    public ResourceNotFoundException(String customMessage) {
        super(ErrorCode.PRESENTATION_NOT_FOUND, customMessage);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public ResourceNotFoundException(String customMessage, Throwable cause) {
        super(ErrorCode.PRESENTATION_NOT_FOUND, customMessage, cause);
    }
}