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
public class StudentPerformanceDto {

    private Double overallAverage;
    private Integer completedAssignments;
    private Integer totalAssignments;
    private Double completionRate;
    private Integer pendingAssignments;
    private Integer overdueAssignments;
    private List<ClassPerformanceSummary> classSummaries;
    private List<PerformanceTrend> performanceTrends;
    private Map<String, Long> gradeDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClassPerformanceSummary {
        private String className;
        private Double averageScore;
        private Integer completedAssignments;
        private Integer totalAssignments;
        private Double completionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerformanceTrend {
        private String period;
        private Double averageScore;
        private Integer submissionCount;
    }
}
