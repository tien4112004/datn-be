package com.datn.datnbe.cms.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDto {
    private String id;
    private String lessonId;
    private String studentId;
    private String content;
    private String mediaUrl;
    private Integer grade;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
