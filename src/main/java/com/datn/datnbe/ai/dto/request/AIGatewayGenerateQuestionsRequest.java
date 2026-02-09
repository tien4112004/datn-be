package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AIGatewayGenerateQuestionsRequest {

    private String topic;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("questions_per_difficulty")
    private Map<String, Integer> questionsPerDifficulty;

    @JsonProperty("question_types")
    private List<String> questionTypes;

    @JsonProperty("prompt")
    private String prompt;

    private String provider;
    private String model;
}
