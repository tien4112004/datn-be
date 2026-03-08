package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request sent to GenAI Gateway for single-topic question generation.
 * <p>
 * Contains one or two groups:
 * <ul>
 *   <li>CONTEXT group — questions backed by a reading passage</li>
 *   <li>NORMAL group  — questions from curriculum knowledge only</li>
 * </ul>
 * The gateway returns a JSON array with a {@code group} index on each question.
 * The backend uses that index to assign {@code contextId} to context-based questions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIGatewayGenerateByTopicRequest {

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("topic_name")
    private String topicName;

    @JsonProperty("groups")
    private List<Group> groups;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;

    // -------------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Group {

        /** "CONTEXT" or "NORMAL". */
        @JsonProperty("group_type")
        private String groupType;

        /** Reading passage content — only present for CONTEXT groups. */
        @JsonProperty("context_content")
        private String contextContent;

        /** "TEXT" or "IMAGE" — only present for CONTEXT groups. */
        @JsonProperty("context_type")
        private String contextType;

        /**
         * Map of difficulty -> questionType -> requirement.
         * Example: {"KNOWLEDGE": {"MULTIPLE_CHOICE": {count:3, points:1.0}}}
         */
        @JsonProperty("questionsPerDifficulty")
        private Map<String, Map<String, GenerateQuestionsFromMatrixRequest.QuestionRequirement>> requirements;
    }
}
