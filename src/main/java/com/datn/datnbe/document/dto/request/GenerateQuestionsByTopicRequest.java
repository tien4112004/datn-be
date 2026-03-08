package com.datn.datnbe.document.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to generate questions for a single topic row from the assignment matrix.
 * <p>
 * The backend determines context splitting: if {@code hasContext=true}, up to 7 questions
 * use a randomly selected reading passage; remaining questions are generated normally.
 * Questions are NOT saved to the question bank — they are returned directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsByTopicRequest {

    @NotBlank(message = "Grade is required")
    private String grade;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Topic name is required")
    private String topicName;

    /**
     * Whether this topic uses context-based questions (reading passages).
     * When true, the backend selects a context from the DB and may split
     * the question requirements between context-based and normal groups.
     */
    @JsonProperty("hasContext")
    private boolean hasContext;

    /**
     * 2D map from the matrix row: difficulty -> questionType -> "count:points".
     * Example: {"KNOWLEDGE": {"MULTIPLE_CHOICE": "3:1.0", "FILL_IN_BLANK": "2:2.0"}}
     */
    @NotNull(message = "questionsPerDifficulty is required")
    private Map<String, Map<String, String>> questionsPerDifficulty;

    /** Optional custom instruction for question generation. */
    private String prompt;

    /** LLM provider (default: google). */
    private String provider;

    /** LLM model (default: gemini-2.5-flash). */
    private String model;
}
