package com.datn.document.exception;

/**
 * Custom exception for document operations
 */
public class DocumentException extends RuntimeException {
    
    public DocumentException(String message) {
        super(message);
    }
    
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
