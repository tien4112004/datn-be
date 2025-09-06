package com.datn.datnbe.sharedkernel.exceptions;

/** Exception thrown when validation fails */
public class ValidationException extends AppException {

    public ValidationException() {
        super(ErrorCode.VALIDATION_ERROR);
    }

    public ValidationException(String customMessage) {
        super(ErrorCode.VALIDATION_ERROR, customMessage);
    }

    public ValidationException(String customMessage, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, customMessage, cause);
    }
}
