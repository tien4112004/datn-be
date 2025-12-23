package com.datn.datnbe.cms.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDto {
    private String id;
    private String classId;
    private String authorId;
    private String content;
    private String type;
    private List<String> attachments;
    private List<String> linkedResourceIds;
    private String linkedLessonId;
    private Boolean isPinned;
    private Boolean allowComments;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
