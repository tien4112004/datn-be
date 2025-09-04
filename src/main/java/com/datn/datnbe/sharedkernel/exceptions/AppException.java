package com.datn.datnbe.sharedkernel.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/** Custom exception for document operations */
@Getter
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AppException extends RuntimeException {
    ErrorCode errorCode;
    String errorMessage;

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
