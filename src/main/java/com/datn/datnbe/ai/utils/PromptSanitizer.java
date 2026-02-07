package com.datn.datnbe.ai.utils;

/**
 * Utility class for sanitizing prompt inputs to prevent template injection attacks.
 * Strips potentially dangerous patterns like ${}, <>, and backticks.
 */
public final class PromptSanitizer {

    private static final int MAX_GRADE_LENGTH = 50;
    private static final int MAX_SUBJECT_LENGTH = 100;

    private PromptSanitizer() {
        // Prevent instantiation
    }

    /**
     * Sanitizes input by removing template injection patterns.
     * Removes: ${...}, <>, backticks, and collapses whitespace.
     *
     * @param input the input string to sanitize
     * @return sanitized string, or null if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\$\\{[^}]*\\}", "")  // Remove ${...} patterns
                .replaceAll("[<>]", "")             // Remove angle brackets
                .replaceAll("`", "")                // Remove backticks
                .replaceAll("\\s+", " ")            // Collapse whitespace
                .trim();
    }

    public static Integer sanitizeGrade(Integer grade) {
        // Integer is already safe, no sanitization needed
        return grade;
    }

    public static String sanitizeSubject(String subject) {
        String sanitized = sanitize(subject);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized.length() > MAX_SUBJECT_LENGTH ? sanitized.substring(0, MAX_SUBJECT_LENGTH) : sanitized;
    }
}
