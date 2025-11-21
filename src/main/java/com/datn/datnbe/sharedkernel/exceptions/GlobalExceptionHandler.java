package com.datn.datnbe.sharedkernel.exceptions;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponseDto<Object>> handleAppException(AppException ex) {
        // Log authentication errors at debug level to reduce noise
        if (ex.getErrorCode() == ErrorCode.AUTH_INVALID_CREDENTIALS
                || ex.getErrorCode() == ErrorCode.AUTH_UNAUTHORIZED) {
            log.debug("Authentication error: {}", ex.getMessage());
        } else {
            log.error("Application exception occurred: {}", ex.getMessage(), ex);
        }

        var response = AppResponseDto.failure(ex);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppResponseDto<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.error("Malformed JSON request: {}", ex.getMessage(), ex);

        AppException appException = new AppException(ErrorCode.VALIDATION_ERROR);
        AppResponseDto<Object> response = AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<AppResponseDto<Object>> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        AppResponseDto<Object> response = AppResponseDto.failure(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponseDto<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        log.error("Validation errors occurred: {}", ex.getMessage());
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        AppResponseDto<Map<String, String>> response = AppResponseDto.<Map<String, String>>builder()
                .success(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponseDto<Object>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        AppException appException = new AppException(ErrorCode.UNCATEGORIZED_ERROR, ex.getMessage());
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        AppResponseDto<Object> response = AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<AppResponseDto<Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeds the maximum limit: {}", ex.getMessage(), ex);

        AppException appException = new AppException(ErrorCode.FILE_TOO_LARGE);
        AppResponseDto<Object> response = AppResponseDto.failure(appException);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex) {
        // Client disconnected or cancelled the request - log at debug level to avoid noise
        log.debug("Client disconnected or cancelled streaming request: {}", ex.getMessage());
    }
}
