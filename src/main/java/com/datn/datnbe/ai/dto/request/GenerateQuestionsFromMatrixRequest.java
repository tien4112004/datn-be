package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to generate questions from matrix (sent to GenAI Gateway).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsFromMatrixRequest {

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("matrix_items")
    private List<MatrixItemWithContext> matrixItems;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;

    /**
     * Matrix item with optional context information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatrixItemWithContext {

        @JsonProperty("topic_index")
        private Integer topicIndex;

        @JsonProperty("topic_name")
        private String topicName;

        @JsonProperty("difficulty")
        private String difficulty;

        @JsonProperty("question_type")
        private String questionType;

        @JsonProperty("count")
        private Integer count;

        @JsonProperty("points")
        private Double points;

        @JsonProperty("context_info")
        private ContextInfo contextInfo;
    }

    /**
     * Information about a selected context.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextInfo {

        @JsonProperty("topic_index")
        private Integer topicIndex;

        @JsonProperty("topic_name")
        private String topicName;

        @JsonProperty("context_id")
        private String contextId;

        @JsonProperty("context_type")
        private String contextType;

        @JsonProperty("context_content")
        private String contextContent;

        @JsonProperty("context_title")
        private String contextTitle;
    }
}
