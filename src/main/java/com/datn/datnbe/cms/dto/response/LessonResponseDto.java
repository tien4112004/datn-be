package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.cms.enums.LessonStatus;
import lombok.Data;

import java.util.Date;

@Data
public class LessonResponseDto {
    private String id;
    private String classId;
    private String className;
    private String subject;
    private String title;
    private String description;
    private Integer duration;
    private LessonStatus status;
    private String notes;
    private String learningObjectives;
    private String ownerId;
    private Date createdAt;
    private Date updatedAt;
}
