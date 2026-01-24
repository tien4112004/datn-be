package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {
    private String id;
    private String classId;
    private String authorId;
    private UserMinimalInfoDto author;
    private String content;
    private String type; // Post, Assignment
    private List<String> attachments;
    private List<LinkedResourceDto> linkedResources;
    private String linkedLessonId;
    private Boolean isPinned;
    private Boolean allowComments;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
