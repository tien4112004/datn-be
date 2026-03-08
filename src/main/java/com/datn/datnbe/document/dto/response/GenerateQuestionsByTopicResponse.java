package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for the generate-questions-by-topic API.
 * <p>
 * Questions are NOT persisted to the question bank — they are returned directly
 * for use in the assignment. Context-based questions have {@code contextId} set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateQuestionsByTopicResponse {

    private int totalGenerated;

    private List<QuestionResponseDto> questions;

    /**
     * The context (reading passage) that was selected when {@code hasContext=true}.
     * Null when no context was used.
     */
    private SelectedContextDto context;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SelectedContextDto {
        private String id;
        private String title;
    }
}
