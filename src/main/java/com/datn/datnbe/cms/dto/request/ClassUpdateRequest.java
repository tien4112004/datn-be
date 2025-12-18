package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassUpdateRequest {

    @Size(max = 50, message = "Class name cannot exceed 50 characters")
    String name;

    String description;

    String settings;

    Boolean isActive;
}
