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

    String description;

    String settings;
}
