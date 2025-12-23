package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.cms.enums.LessonResourceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonResourceResponseDto {
    private String id;
    private String lessonId;
    private String name;
    private LessonResourceType type;
    private String url;
    private String filePath;
    private String description;
    private Boolean isRequired;
    private Boolean isPrepared;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
