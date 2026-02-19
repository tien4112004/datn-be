package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.datn.datnbe.ai.dto.request.GenerateQuestionsFromMatrixRequest.QuestionRequirement;

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
     * Map of difficulty -> questionType -> requirement (count and points).
     * Matches the same structure used by GenerateQuestionsFromMatrixRequest.TopicRequirement.
     */
    @JsonProperty("questionsPerDifficulty")
    private Map<String, Map<String, QuestionRequirement>> questionsPerDifficulty;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("model")
    private String model;
}
