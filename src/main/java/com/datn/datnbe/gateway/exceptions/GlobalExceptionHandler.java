package com.datn.datnbe.gateway.exceptions;

import com.datn.datnbe.ai.dto.response.common.AppResponseDto;
import com.datn.datnbe.document.src.management.exception.ErrorCode;
import com.datn.datnbe.document.src.management.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponseDto<Object>> handleAppException(AppException ex) {
        var response = AppResponseDto.failure(ex);
        log.error(response.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        com.datn.datnbe.document.src.management.exception.AppException appException = new com.datn.datnbe.document.src.management.exception.AppException(ErrorCode.VALIDATION_ERROR);
        com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object> response = com.datn.datnbe.document.src.management.dto.common.AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.datn.datnbe.document.src.management.exception.ValidationException.class)
    public ResponseEntity<com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object>> handleValidationException(ValidationException ex) {
        com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object> response = com.datn.datnbe.document.src.management.dto.common.AppResponseDto.failure(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Map<String, String>> response = com.datn.datnbe.document.src.management.dto.common.AppResponseDto.<Map<String, String>>builder()
                .status("error")
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object>> handleGenericException(Exception ex) {
        com.datn.datnbe.document.src.management.exception.AppException appException = new com.datn.datnbe.document.src.management.exception.AppException(ErrorCode.UNCATEGORIZED_ERROR, ex.getMessage());
        com.datn.datnbe.document.src.management.dto.common.AppResponseDto<Object> response = com.datn.datnbe.document.src.management.dto.common.AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
