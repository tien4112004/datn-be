package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.document.entity.Question;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from generating questions from an assignment matrix.
 * Can contain either raw JSON from GenAI Gateway or parsed structured data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsFromMatrixResponse {

    /**
     * Raw JSON response from GenAI Gateway (if not yet parsed).
     * Backend will parse this to extract questions and fill in common fields.
     */
    private String rawJson;

    /**
     * Topics with their generated questions and optional context (after parsing).
     */
    private List<TopicWithQuestionsDto> topics;

    /**
     * Total number of questions generated across all topics.
     */
    @JsonProperty("totalQuestions")
    private Integer totalQuestions;

    /**
     * A topic with its generated questions and optional context information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicWithQuestionsDto {

        /**
         * Index of the topic in the matrix.
         */
        @JsonProperty("topicIndex")
        private Integer topicIndex;

        /**
         * Name of the topic.
         */
        @JsonProperty("topicName")
        private String topicName;

        /**
         * Context information if this is a context-based topic.
         * Null for regular curriculum topics.
         */
        private UsedContextDto context;

        /**
         * Questions generated for this topic.
         */
        private List<Question> questions;
    }

    /**
     * Information about a context that was used for question generation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsedContextDto {

        /**
         * ID of the context that was used.
         */
        @JsonProperty("contextId")
        private String contextId;

        /**
         * Title of the context for display purposes.
         */
        @JsonProperty("contextTitle")
        private String contextTitle;

        /**
         * Type of context (TEXT, IMAGE, etc.).
         */
        @JsonProperty("contextType")
        private String contextType;

        /**
         * The actual context content (text or base64 image).
         */
        @JsonProperty("contextContent")
        private String contextContent;
    }
}
