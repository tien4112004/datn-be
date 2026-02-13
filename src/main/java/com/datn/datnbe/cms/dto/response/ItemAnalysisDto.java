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
public class ItemAnalysisDto {

    private String assignmentId;
    private String assignmentTitle;
    private Integer totalSubmissions;
    private List<QuestionAnalysis> questionAnalysis;
    private List<TopicAnalysis> topicAnalysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionAnalysis {
        private String questionId;
        private String questionTitle;
        private String questionType;
        private Double averageScore;
        private Double maxPoints;
        private Double successRate;
        private Integer correctCount;
        private Integer incorrectCount;
        private String difficulty;
        private Map<String, Integer> optionDistribution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopicAnalysis {
        private String topicId;
        private String topicName;
        private Double averageScore;
        private Double successRate;
        private Integer questionCount;
    }
}
