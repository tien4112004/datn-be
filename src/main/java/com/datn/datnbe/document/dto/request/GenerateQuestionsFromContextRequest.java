package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to generate questions from a specific context and save to question bank.
 * Uses the same matrix cell format: difficulty -> questionType -> "count:points"
 * Example: {"KNOWLEDGE": {"MULTIPLE_CHOICE": "3:1.0", "FILL_IN_BLANK": "2:2.0"}}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsFromContextRequest {

    /** Context ID (optional - used when context exists in DB). */
    private String contextId;

    /** Inline context content (used when context may not exist in DB). */
    private String contextContent;

    /** Context title (used with inline content). */
    private String contextTitle;

    /** Grade level (used with inline content, e.g., "1", "2", "3", "4", "5"). */
    private String grade;

    /** Subject code (used with inline content, e.g., "T", "TV", "TA"). */
    private String subject;

    /**
     * Map of difficulty -> questionType -> "count:points" string.
     * Same cell format as the 3D assignment matrix.
     */
    @NotNull
    private Map<String, Map<String, String>> questionsPerDifficulty;

    /** Optional guidelines for the AI (e.g., "focus on comprehension skills"). */
    private String prompt;

    private String provider;

    private String model;
}
