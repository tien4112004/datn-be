package com.datn.datnbe.ai.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for generic AI image generation endpoint.
 * Contains a ready-to-use prompt (no building logic in ai-worker).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateRequest {
    /**
     * Complete, ready-to-use image generation prompt.
     * Already includes description, style, theme, and formatting.
     */
    private String prompt;

    /**
     * AI model to use for image generation.
     * Example: "gemini-2.5-flash-image"
     */
    private String model;

    /**
     * AI provider name.
     * Example: "google"
     */
    private String provider;

    /**
     * Number of images to generate.
     * Default: 1
     */
    private Integer numberOfImages = 1;

    /**
     * Image aspect ratio.
     * Options: "1:1", "9:16", "16:9", "4:3", "3:4"
     * Default: "16:9" for presentations
     */
    private String aspectRatio = "16:9";

    /**
     * Safety filter level.
     * Options: "block_none", "block_few", "block_some", "block_most"
     * Default: "block_few"
     */
    private String safetyFilterLevel = "block_few";

    /**
     * Person generation policy.
     * Options: "allow_all", "allow_adult", "dont_allow"
     * Default: "allow_all"
     */
    private String personGeneration = "allow_all";

    /**
     * Random seed for reproducible generation (optional).
     */
    private Integer seed;

    /**
     * Negative prompt to avoid certain elements (optional).
     */
    private String negativePrompt;
}
