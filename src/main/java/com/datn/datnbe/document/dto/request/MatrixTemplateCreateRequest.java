package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new matrix template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixTemplateCreateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Subject is required")
    @Size(max = 50, message = "Subject must not exceed 50 characters")
    private String subject;

    @NotBlank(message = "Grade is required")
    @Size(max = 10, message = "Grade must not exceed 10 characters")
    private String grade;

    @NotBlank(message = "Matrix data is required")
    private String matrixData;
}
