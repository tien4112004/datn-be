package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Remove NON_NULL to always include all fields
public class GradingQueueItemDto {

    private String submissionId;
    private String assignmentId;
    private String assignmentTitle;
    private String postId;
    private String classId;
    private String className;
    private UserMinimalInfoDto student;
    private Instant submittedAt;
    private Long daysSinceSubmission;
    private String status;
    private Double autoGradedScore;
    private Double maxScore;
}
