package com.datn.aiservice.dto.response.common;
import com.datn.aiservice.exceptions.AppException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tkpm.sms.domain.exception.DomainException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponseDto<T> {
    @Builder.Default
    int code = HttpStatus.OK.value();

    String message;
    String errorCode;
    T content;

    public static <T> AppResponseDto<T> success(T content, String message) {
        return AppResponseDto.<T>builder().message(message).content(content).build();
    }

    public static <T> AppResponseDto<T> success(T content) {
        return AppResponseDto.<T>builder().content(content).build();
    }

    public static <T> AppResponseDto<T> success(String message) {
        return AppResponseDto.<T>builder().message(message).content(null).build();
    }

    public static <T> AppResponseDto<T> success() {
        return AppResponseDto.<T>builder().content(null).build();
    }

    public static <T> AppResponseDto<T> failure(AppException exception) {
        return AppResponseDto.<T>builder().code(500).message(exception.getMessage())
                .errorCode(exception.getErrorCode().getErrorCodeName()).build();
    }

    public static <T> AppResponseDto<T> failure(AppException exception, int status) {
        return AppResponseDto.<T>builder().code(status).message(exception.getMessage())
                .errorCode(exception.getErrorCode().getErrorCodeName()).build();
    }
}