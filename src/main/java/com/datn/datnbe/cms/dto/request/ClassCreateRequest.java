package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassCreateRequest {

    @NotBlank(message = "Class name is required")
    @Size(max = 50, message = "Class name cannot exceed 50 characters")
    String name;

    @NotNull(message = "Grade is required")
    @Min(value = 1, message = "Grade must be at least 1")
    @Max(value = 5, message = "Grade cannot exceed 5")
    Integer grade;

    @NotBlank(message = "Academic year is required")
    @Size(max = 9, message = "Academic year cannot exceed 9 characters")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in format YYYY-YYYY")
    String academicYear;

    @Size(max = 100, message = "Classroom cannot exceed 100 characters")
    String classroom;

    String description;
}
