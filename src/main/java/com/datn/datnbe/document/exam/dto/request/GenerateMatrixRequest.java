package com.datn.datnbe.document.exam.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @Schema(description = "List of topic names to include in the matrix", example = "[\"Algebra\", \"Geometry\", \"Calculus\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one topic is required")
    private List<String> topics;

    @Schema(description = "Grade level for the exam", example = "Grade 10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("gradeLevel")
    @JsonAlias("grade_level")
    @NotBlank(message = "Grade level is required")
    private String gradeLevel;

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

    @Schema(description = "Difficulty levels to include", example = "[\"easy\", \"medium\", \"hard\"]", defaultValue = "[\"easy\", \"medium\", \"hard\"]")
    @Builder.Default
    private List<String> difficulties = Arrays.asList("easy", "medium", "hard");

    @Schema(description = "Question types to include", example = "[\"multiple_choice\", \"true_false\"]", defaultValue = "[\"multiple_choice\", \"fill_in_blank\", \"true_false\", \"matching\"]")
    @JsonProperty("questionTypes")
    @JsonAlias("question_types")
    @Builder.Default
    private List<String> questionTypes = Arrays.asList("multiple_choice", "fill_in_blank", "true_false", "matching");

    @Schema(description = "Additional requirements or context for the exam", example = "Focus on problem-solving skills")
    @JsonProperty("additionalRequirements")
    @JsonAlias("additional_requirements")
    private String additionalRequirements;

    @Schema(description = "AI provider to use", example = "google", defaultValue = "google", allowableValues = {
            "google", "openai", "openrouter"})
    @Builder.Default
    private String provider = "google";

    @Schema(description = "AI model to use", example = "gemini-2.5-flash", defaultValue = "gemini-2.0-flash-exp")
    @Builder.Default
    private String model = "gemini-2.0-flash-exp";
}
