package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    @NotBlank
    private String content;

    private String type; // Post, Assignment

    private List<String> attachments;

    @Valid
    private List<LinkedResourceDto> linkedResources;

    private String linkedLessonId;

    private Boolean allowComments;
}
