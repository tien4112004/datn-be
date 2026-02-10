package com.datn.datnbe.sharedkernel.dto;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponseDto<T> {
    @Builder.Default
    boolean success = true;
    @Builder.Default
    int code = HttpStatus.OK.value();
    @Builder.Default
    Date timestamp = new Date();

    T data;
    String message;
    String errorCode;
    PaginationDto pagination;

    public static <T> AppResponseDto<T> success() {
        return AppResponseDto.<T>builder().build();
    }

    public static <T> AppResponseDto<T> success(T data) {
        return AppResponseDto.<T>builder().data(data).build();
    }

    public static <T> AppResponseDto<T> success(String message) {
        return AppResponseDto.<T>builder().message(message).data(null).build();
    }

    public static <T> AppResponseDto<T> success(T data, String message) {
        return AppResponseDto.<T>builder().message(message).data(data).build();
    }

    public static <T> AppResponseDto<T> successWithPagination(T data, PaginationDto pagination) {
        return AppResponseDto.<T>builder().data(data).pagination(pagination).build();
    }

    public static <T> AppResponseDto<T> failure(AppException exception) {
        return AppResponseDto.<T>builder()
                .success(false)
                .code(exception.getStatusCode())
                .message(exception.getErrorMessage())
                .errorCode(exception.getErrorCode().getErrorCodeName())
                .build();
    }

    public static <T> AppResponseDto<T> failure(AppException exception, int status) {
        return AppResponseDto.<T>builder()
                .code(status)
                .message(exception.getMessage())
                .errorCode(exception.getErrorCode().getErrorCodeName())
                .build();
    }
}
