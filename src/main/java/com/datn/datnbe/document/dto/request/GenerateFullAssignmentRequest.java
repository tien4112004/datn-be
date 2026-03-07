package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to generate a full assignment with AI-generated questions.
 * Supports both context-based and regular curriculum questions.
 *
 * <p>Difference from question bank selection:
 * <ul>
 *   <li>This endpoint GENERATES new questions using AI</li>
 *   <li>/generate-from-matrix SELECTS existing questions from question bank</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFullAssignmentRequest {

    /**
     * ID of saved assignment matrix (mutually exclusive with matrix field).
     * Either matrixId OR matrix must be provided.
     */
    private String matrixId;

    /**
     * Inline assignment matrix (mutually exclusive with matrixId field).
     * Either matrixId OR matrix must be provided.
     */
    private AssignmentMatrixDto matrix;

    /**
     * Title of the assignment (required).
     */
    @NotBlank(message = "Assignment title is required")
    private String title;

    /**
     * Description of the assignment (optional).
     */
    private String description;

    /**
     * Grade level (optional).
     */
    @Size(max = 50)
    private String grade;

    /**
     * Subject (optional).
     */
    @Size(max = 100)
    private String subject;

    /**
     * Chapter (optional).
     */
    @Size(max = 255)
    private String chapter;

    /**
     * AI provider to use for question generation.
     * Defaults to "google" if not specified.
     */
    private String provider;

    /**
     * AI model to use for question generation.
     * Defaults to "gemini-2.5-flash" if not specified.
     */
    private String model;
}
