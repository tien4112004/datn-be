package com.datn.datnbe.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified response wrapper for AI-generated questions.
 * Contains raw JSON string that will be parsed later in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIGeneratedQuestionsResponse {

    /**
     * Raw JSON string containing array of generated questions
     */
    private String result;

    /**
     * Number of questions generated
     */
    private int count;
}
