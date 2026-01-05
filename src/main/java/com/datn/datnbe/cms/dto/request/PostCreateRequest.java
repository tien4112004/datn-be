package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    @NotBlank
    private String content;

    private String type; // Post, Assignment

    private List<String> attachments;

    private List<String> linkedResourceIds;

    private String linkedLessonId;

    private Boolean allowComments;
}
