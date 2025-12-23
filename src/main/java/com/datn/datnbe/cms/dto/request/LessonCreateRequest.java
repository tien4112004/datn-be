package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonCreateRequest {
    private String classId;

    @NotBlank
    private String subject;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer duration;

    private String notes;

    private String learningObjectives; // JSON string
}
