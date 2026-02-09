package com.datn.datnbe.document.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Request to generate a 3D exam matrix using AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate a 3D exam matrix using AI")
public class GenerateMatrixRequest {

    @Schema(description = "Name for the exam matrix", example = "Math Final Exam Matrix", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "List of curriculum chapters (automatically populated by backend, not provided by user)", hidden = true, example = "[\"Chapter 1: Numbers\", \"Chapter 2: Geometry\"]")
    private List<String> chapters;

    @Schema(description = "Grade level for the exam", example = "Grade 10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("grade")
    @JsonAlias("grade_level")
    @NotBlank(message = "Grade level is required")
    private String grade;

    @Schema(description = "Subject for the exam (T, TV, TA)", example = "T", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("subject")
    @JsonAlias("subject")
    @NotBlank(message = "Subject is required")
    private String subject;

    @Schema(description = "Target total number of questions", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("totalQuestions")
    @JsonAlias("total_questions")
    @NotNull(message = "Total questions is required")
    @Min(value = 1, message = "Total questions must be at least 1")
    private Integer totalQuestions;

    @Schema(description = "Target total points for the exam", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("totalPoints")
    @JsonAlias("total_points")
    @NotNull(message = "Total points is required")
    @Min(value = 1, message = "Total points must be at least 1")
    private Integer totalPoints;

    @Schema(description = "Difficulty levels to include", example = "[\"KNOWLEDGE\", \"COMPREHENSION\", \"APPLICATION\"]", defaultValue = "[\"KNOWLEDGE\", \"COMPREHENSION\", \"APPLICATION\"]")
    @Builder.Default
    private List<String> difficulties = Arrays.asList("KNOWLEDGE", "COMPREHENSION", "APPLICATION");

    @Schema(description = "Question types to include", example = "[\"MULTIPLE_CHOICE\", \"OPEN_ENDED\"]", defaultValue = "[\"MULTIPLE_CHOICE\", \"FILL_IN_BLANK\", \"OPEN_ENDED\", \"MATCHING\"]")
    @JsonProperty("questionTypes")
    @JsonAlias("question_types")
    @Builder.Default
    private List<String> questionTypes = Arrays.asList("MULTIPLE_CHOICE", "FILL_IN_BLANK", "OPEN_ENDED", "MATCHING");

    @Schema(description = "Prompt or context for the exam", example = "Focus on problem-solving skills")
    @JsonProperty("prompt")
    @JsonAlias("prompt")
    private String prompt;

    @Schema(description = "Language for AI responses (vi for Vietnamese, en for English)", example = "vi", defaultValue = "vi", allowableValues = {
            "vi", "en"})
    @Builder.Default
    private String language = "vi";

    @Schema(description = "AI provider to use", example = "google", defaultValue = "google", allowableValues = {
            "google", "openai", "openrouter"})
    @Builder.Default
    private String provider = "google";

    @Schema(description = "AI model to use", example = "gemini-2.5-flash", defaultValue = "gemini-2.5-flash-lite")
    @Builder.Default
    private String model = "gemini-2.5-flash-lite";
}
