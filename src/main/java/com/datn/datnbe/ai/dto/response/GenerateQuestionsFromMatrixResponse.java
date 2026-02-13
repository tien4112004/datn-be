package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.document.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from generating questions from an assignment matrix.
 * Includes the generated questions and information about which contexts were used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsFromMatrixResponse {

    /**
     * List of generated questions.
     */
    private List<Question> questions;

    /**
     * List of contexts that were randomly selected for context-based topics.
     * Empty list if no context-based topics were in the matrix.
     */
    private List<UsedContextDto> usedContexts;

    /**
     * Total number of questions generated.
     */
    private Integer totalQuestions;

    /**
     * Information about a context that was used for question generation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsedContextDto {

        /**
         * Index of the topic in the matrix that used this context.
         */
        private Integer topicIndex;

        /**
         * ID of the context that was randomly selected.
         */
        private String contextId;

        /**
         * Title of the context for display purposes.
         */
        private String contextTitle;
    }
}
