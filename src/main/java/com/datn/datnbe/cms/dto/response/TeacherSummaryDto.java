package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherSummaryDto {

    private Integer totalClasses;
    private Integer totalStudents;
    private Integer totalAssignments;
    private Integer pendingGrading;
    private Double averageClassScore;
    private Double engagementRate24h;
    private Map<String, Long> overallGradeDistribution;
    private ComparisonMetrics comparison;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonMetrics {
        private Double scoreChange;
        private Double engagementChange;
        private Double submissionChange;
    }
}
