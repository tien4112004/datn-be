package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithContextDto {

    private QuestionContextDto context;

    @JsonProperty("question_number")
    private Integer questionNumber;

    private String topic;

    @JsonProperty("grade_level")
    private String gradeLevel;

    private String difficulty;

    private QuestionDto question;

    @JsonProperty("default_points")
    private Integer defaultPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionContextDto {
        @JsonProperty("context_type")
        private String contextType;

        private String title;
        private String content;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        @JsonProperty("question_type")
        private String questionType;

        private String content;
        private Object answers;

        @JsonProperty("correct_answer")
        private Object correctAnswer;

        private String explanation;
    }
}
