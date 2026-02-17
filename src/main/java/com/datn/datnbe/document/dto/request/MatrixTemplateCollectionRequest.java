package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for listing matrix templates with pagination and filtering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixTemplateCollectionRequest {

    @Min(value = 1, message = "Page must be at least 1")
    @Builder.Default
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer pageSize = 10;

    @Size(max = 50, message = "Subject must not exceed 50 characters")
    private String subject;

    @Size(max = 10, message = "Grade must not exceed 10 characters")
    private String grade;

    @Size(max = 255, message = "Search must not exceed 255 characters")
    private String search;

    @Size(max = 20, message = "Bank type must not exceed 20 characters")
    private String bankType;  // "personal" or "public"
}
