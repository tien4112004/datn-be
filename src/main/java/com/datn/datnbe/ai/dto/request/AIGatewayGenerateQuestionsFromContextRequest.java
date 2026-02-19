package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO sent to GenAI Gateway for context-based question generation.
 * Maps to GenAI Gateway's GenerateQuestionsFromContextRequest schema.
 *
 * questionsPerDifficulty uses the same matrix cell format:
 * difficulty -> questionType -> "count:points"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIGatewayGenerateQuestionsFromContextRequest {

    @JsonProperty("context")
    private String context;

    @JsonProperty("context_type")
    private String contextType;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("subject")
    private String subject;

    /**
     * Map of difficulty -> questionType -> "count:points" string.
     * Sent as "questionsPerDifficulty" (camelCase) to match GenAI Gateway alias.
     */
    @JsonProperty("questionsPerDifficulty")
    private Map<String, Map<String, String>> questionsPerDifficulty;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;
}
