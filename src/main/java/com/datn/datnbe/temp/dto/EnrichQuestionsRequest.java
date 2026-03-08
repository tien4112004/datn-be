package com.datn.datnbe.temp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * TEMPORARY — Remove this class after database enrichment is complete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichQuestionsRequest {

    @NotBlank
    private String grade;

    @NotBlank
    private String subject;

    @NotBlank
    private String chapter;

    /**
     * Number of questions per difficulty.
     * Example: {"KNOWLEDGE": 3, "COMPREHENSION": 2}
     */
    @NotNull
    private Map<String, Integer> questionsPerDifficulty;

    /**
     * Question types. Only MULTIPLE_CHOICE and MATCHING produce useful visual questions.
     * Defaults to MULTIPLE_CHOICE only if omitted.
     */
    private List<String> questionTypes;

    private String prompt;

    private String provider;

    private String model;

    private String imageModel;

    private String imageProvider;

    private String imageAspectRatio;
}
