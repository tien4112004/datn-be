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

    /**
     * Sanitizes grade input with length limit.
     *
     * @param grade the grade string to sanitize
     * @return sanitized grade limited to 50 characters, or null if input is null or empty after sanitization
     */
    public static String sanitizeGrade(String grade) {
        String sanitized = sanitize(grade);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized.length() > MAX_GRADE_LENGTH ? sanitized.substring(0, MAX_GRADE_LENGTH) : sanitized;
    }

    /**
     * Sanitizes subject input with length limit.
     *
     * @param subject the subject string to sanitize
     * @return sanitized subject limited to 100 characters, or null if input is null or empty after sanitization
     */
    public static String sanitizeSubject(String subject) {
        String sanitized = sanitize(subject);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized.length() > MAX_SUBJECT_LENGTH ? sanitized.substring(0, MAX_SUBJECT_LENGTH) : sanitized;
    }
}
