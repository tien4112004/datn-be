package com.datn.datnbe.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a gap in the exam matrix that couldn't be filled
 * from the question bank.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixGapDto {

    /**
     * Topic name for the gap.
     */
    private String topic;

    /**
     * Difficulty level for the gap.
     */
    private String difficulty;

    /**
     * Question type for the gap.
     */
    private String questionType;

    /**
     * Number of questions required by the matrix.
     */
    private Integer requiredCount;

    /**
     * Number of questions available in the question bank.
     */
    private Integer availableCount;

    /**
     * Number of questions still needed.
     */
    public Integer getGapCount() {
        return Math.max(0, requiredCount - availableCount);
    }
}
