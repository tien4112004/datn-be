package com.datn.datnbe.gateway.exceptions;

import com.datn.datnbe.document.src.management.exception.AppException;
import com.datn.datnbe.document.src.management.exception.ErrorCode;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException() {
        super(com.datn.datnbe.document.src.management.exception.ErrorCode.PRESENTATION_NOT_FOUND);
    }

    public ResourceNotFoundException(String customMessage) {
        super(com.datn.datnbe.document.src.management.exception.ErrorCode.PRESENTATION_NOT_FOUND, customMessage);
    }

    public ResourceNotFoundException(com.datn.datnbe.document.src.management.exception.ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public ResourceNotFoundException(String customMessage, Throwable cause) {
        super(ErrorCode.PRESENTATION_NOT_FOUND, customMessage, cause);
    }
}