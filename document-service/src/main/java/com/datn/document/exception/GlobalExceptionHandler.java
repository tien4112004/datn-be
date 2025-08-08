package com.datn.document.exception;

import com.datn.document.dto.common.AppResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for document service
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponseDto<Object>> handleAppException(AppException ex) {
        AppResponseDto<Object> response = AppResponseDto.failure(ex);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppResponseDto<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        AppException appException = new AppException(ErrorCode.VALIDATION_ERROR);
        AppResponseDto<Object> response = AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<AppResponseDto<Object>> handleValidationException(ValidationException ex) {
        AppResponseDto<Object> response = AppResponseDto.failure(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponseDto<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        AppResponseDto<Map<String, String>> response = AppResponseDto.<Map<String, String>>builder()
                .status("error")
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponseDto<Object>> handleGenericException(Exception ex) {
        AppException appException = new AppException(ErrorCode.UNCATEGORIZED_ERROR, ex.getMessage());
        AppResponseDto<Object> response = AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
