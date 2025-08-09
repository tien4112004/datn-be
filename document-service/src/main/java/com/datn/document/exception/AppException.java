package com.datn.document.exception;

import lombok.Getter;

/**
 * Custom exception for document operations
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String errorMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDefaultMessage();
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.errorMessage = customMessage;
    }

    public AppException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = customMessage;
    }

    public int getStatusCode() {
        return errorCode.getStatusCode();
    }
}
