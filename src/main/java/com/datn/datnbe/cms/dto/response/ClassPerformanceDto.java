package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassPerformanceDto {

    private String classId;
    private String className;
    private Integer totalStudents;
    private Integer activeStudents;
    private Double participationRate;
    private Double averageScore;
    private Map<String, Long> gradeDistribution;
    private List<AtRiskStudentDto> atRiskStudents;
    private List<AssignmentSummaryDto> recentAssignments;
    private EngagementMetrics engagement;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementMetrics {
        private Double last24Hours;
        private Double last7Days;
        private Double avgSubmissionsPerStudent;
    }
}
