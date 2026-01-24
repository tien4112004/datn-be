package com.datn.datnbe.document.exam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for an exam draft generated from a matrix.
 * Contains selected questions and any gaps that couldn't be filled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraftDto {

    /**
     * Unique identifier for the draft exam.
     */
    private UUID examId;

    /**
     * Title of the exam.
     */
    private String title;

    /**
     * Description of the exam.
     */
    private String description;

    /**
     * List of selected questions for the exam.
     */
    private List<ExamQuestionDto> selectedQuestions;

    /**
     * List of gaps (unfilled requirements from the matrix).
     */
    private List<MatrixGapDto> missingQuestions;

    /**
     * Total points of selected questions.
     */
    private Double totalPoints;

    /**
     * Total number of selected questions.
     */
    private Integer totalQuestions;

    /**
     * Whether the exam is complete (all matrix requirements filled).
     */
    @JsonProperty("isComplete")
    private Boolean isComplete;

    /**
     * Time limit in minutes (if set).
     */
    private Integer timeLimitMinutes;
}
