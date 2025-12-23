package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonUpdateRequest {
    private String classId;

    @NotBlank
    private String subject;

    @NotBlank
    private String title;

    private String description;

    private Integer duration;

    private String notes;

    private String learningObjectives; // JSON string
}
