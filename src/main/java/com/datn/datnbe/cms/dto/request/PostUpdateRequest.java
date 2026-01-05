package com.datn.datnbe.cms.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private String content;
    private String type; // Post, Assignment
    private List<String> attachments;
    private List<String> linkedResourceIds;
    private String linkedLessonId;
    private Boolean isPinned;
    private Boolean allowComments;
}
