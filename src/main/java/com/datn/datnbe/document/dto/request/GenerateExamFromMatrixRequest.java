package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.ExamMatrixDto;
import com.datn.datnbe.document.enums.MissingQuestionStrategy;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request to generate an exam from a matrix by selecting questions
 * from the question bank.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExamFromMatrixRequest {

    /**
     * Reference to a saved matrix (if using saved matrix).
     */
    private UUID matrixId;

    /**
     * Inline matrix (if not using saved matrix).
     * Either matrixId or matrix must be provided.
     */
    private ExamMatrixDto matrix;

    /**
     * Subject for the exam (T, TV, TA).
     */
    @NotBlank(message = "Subject is required")
    @JsonProperty("subject")
    @JsonAlias("subject")
    private String subject;

    /**
     * Title for the exam.
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Description for the exam.
     */
    private String description;

    /**
     * Time limit in minutes (optional).
     */
    @JsonProperty("timeLimitMinutes")
    @JsonAlias("time_limit_minutes")
    private Integer timeLimitMinutes;

    /**
     * Strategy for handling missing questions.
     * - REPORT_GAPS: Return draft with gaps indicated
     * - GENERATE_WITH_AI: Use AI to generate missing questions
     * - FAIL_FAST: Reject if not all requirements can be met
     */
    @JsonProperty("missingStrategy")
    @JsonAlias("missing_strategy")
    @Builder.Default
    private MissingQuestionStrategy missingStrategy = MissingQuestionStrategy.REPORT_GAPS;

    /**
     * Whether to include only public questions or also the teacher's personal questions.
     */
    @JsonProperty("includePersonalQuestions")
    @JsonAlias("include_personal_questions")
    @Builder.Default
    private Boolean includePersonalQuestions = true;

    /**
     * AI provider to use for GENERATE_WITH_AI strategy (optional).
     * Default: "google"
     */
    @Builder.Default
    private String provider = "google";

    /**
     * AI model to use for GENERATE_WITH_AI strategy (optional).
     * Default: "gemini-2.5-flash-lite"
     */
    @Builder.Default
    private String model = "gemini-2.5-flash-lite";
}
