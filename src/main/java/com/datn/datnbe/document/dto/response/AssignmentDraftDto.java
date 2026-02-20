package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.entity.Question;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for an exam/assignment draft generated from a matrix.
 * Follows the Assignment entity schema closely.
 * Contains selected questions and any gaps that couldn't be filled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDraftDto {

    /**
     * Unique identifier for the draft (maps to Assignment.id).
     */
    private String id;

    /**
     * Title of the assignment.
     */
    private String title;

    /**
     * Description of the assignment.
     */
    private String description;

    /**
     * Owner/teacher ID (maps to Assignment.ownerId).
     */
    private String ownerId;

    /**
     * Subject code (maps to Assignment.subject).
     */
    private String subject;

    /**
     * Grade level (maps to Assignment.grade).
     */
    private String grade;

    /**
     * List of selected questions (maps to Assignment.questions).
     * Uses the Question entity directly to match Assignment schema.
     */
    private List<Question> questions;

    /**
     * List of gaps (unfilled requirements from the matrix).
     * This is additional metadata not stored in Assignment.
     */
    private List<MatrixGapDto> missingQuestions;

    /**
     * Total points of selected questions (calculated field).
     */
    private Double totalPoints;

    /**
     * Total number of selected questions (calculated field).
     */
    private Integer totalQuestions;

    /**
     * Whether the exam is complete (all matrix requirements filled).
     */
    @JsonProperty("isComplete")
    private Boolean isComplete;
}
