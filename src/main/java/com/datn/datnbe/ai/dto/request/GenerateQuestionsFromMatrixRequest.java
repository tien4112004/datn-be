package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request to generate questions from matrix (sent to GenAI Gateway).
 * Updated schema to match GenAI Gateway's TopicRequirement structure.
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

    @JsonProperty("topics")
    private List<TopicRequirement> topics;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;

    /**
     * Requirements for a single topic with optional context.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicRequirement {

        @JsonProperty("topic_index")
        private Integer topicIndex;

        @JsonProperty("topic_name")
        private String topicName;

        @JsonProperty("context_info")
        private ContextInfo contextInfo;

        /**
         * Map of difficulty -> question_type -> requirement.
         * Example: {"KNOWLEDGE": {"MULTIPLE_CHOICE": {count: 5, points: 1.0}}}
         */
        @JsonProperty("questionsPerDifficulty")
        private Map<String, Map<String, QuestionRequirement>> questionsPerDifficulty;
    }

    /**
     * Requirement for a specific difficulty and question type.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequirement {

        @JsonProperty("count")
        private Integer count;

        @JsonProperty("points")
        private Double points;
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

    // ============================================================================
    // Deprecated inner classes (for backward compatibility)
    // ============================================================================

    /**
     * @deprecated Use TopicRequirement with questionsPerDifficulty map instead.
     * This flat structure is no longer used by the GenAI Gateway.
     */
    @Deprecated
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
}
