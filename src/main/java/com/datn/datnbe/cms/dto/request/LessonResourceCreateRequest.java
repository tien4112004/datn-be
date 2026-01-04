package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonResourceCreateRequest {
    @NotBlank
    private String lessonId;

    @NotBlank
    private String name;

    @NotBlank
    private String type; // LINK, FILE, EMBED, OTHER

    private String url;

    private String filePath;

    private String description;

    private Boolean isRequired;

    private Boolean isPrepared;
}
