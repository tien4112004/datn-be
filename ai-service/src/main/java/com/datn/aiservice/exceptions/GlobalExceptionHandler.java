package com.datn.aiservice.exceptions;

import com.datn.aiservice.dto.response.common.AppResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponseDto<Object>> handleAppException(AppException ex) {
        var response = AppResponseDto.failure(ex);
        log.error(response.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
}
