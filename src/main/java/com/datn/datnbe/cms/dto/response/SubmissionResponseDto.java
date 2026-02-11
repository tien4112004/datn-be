package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.entity.answerData.AnswerData;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponseDto {
    private String id;
    private String postId;
    private String studentId;
    private String content;
    private List<AnswerData> questions;
    private String mediaUrl;
    private Integer grade;
    private String status;
    private String assignmentId;
    private UserMinimalInfoDto student;
    private UserMinimalInfoDto gradedByUser;
    private LocalDateTime gradedAt;
    private Double score;
    private Integer maxScore;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
