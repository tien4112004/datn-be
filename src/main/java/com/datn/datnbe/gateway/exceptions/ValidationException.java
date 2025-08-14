package com.datn.datnbe.gateway.exceptions;

import com.datn.datnbe.document.src.management.exception.AppException;
import com.datn.datnbe.document.src.management.exception.ErrorCode;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends AppException {

    public ValidationException() {
        super(com.datn.datnbe.document.src.management.exception.ErrorCode.VALIDATION_ERROR);
    }

    public ValidationException(String customMessage) {
        super(com.datn.datnbe.document.src.management.exception.ErrorCode.VALIDATION_ERROR, customMessage);
    }

    public ValidationException(String customMessage, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, customMessage, cause);
    }
}