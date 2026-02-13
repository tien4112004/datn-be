package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecentActivityDto {

    private String id;
    private ActivityType type;
    private UserMinimalInfoDto student;
    private String assignmentTitle;
    private String assignmentId;
    private String className;
    private String classId;
    private Instant timestamp;
    private Double score;
    private String status;

    public enum ActivityType {
        SUBMISSION, GRADING_COMPLETED, LATE_SUBMISSION, RESUBMISSION
    }
}
