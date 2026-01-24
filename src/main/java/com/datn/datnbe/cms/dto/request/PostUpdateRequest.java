package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private String content;
    private String type; // Post, Assignment
    private List<String> attachments;
    @Valid
    private List<LinkedResourceDto> linkedResources;
    private String linkedLessonId;
    private Boolean isPinned;
    private Boolean allowComments;
}
