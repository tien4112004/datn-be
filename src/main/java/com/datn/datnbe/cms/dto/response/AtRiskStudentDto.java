package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtRiskStudentDto {

    private UserMinimalInfoDto student;
    private Double averageScore;
    private Integer missedSubmissions;
    private Integer lateSubmissions;
    private Integer totalAssignments;
    private RiskLevel riskLevel;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
