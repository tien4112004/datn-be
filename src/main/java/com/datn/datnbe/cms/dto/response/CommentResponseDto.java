package com.datn.datnbe.cms.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private String id;
    private String postId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
