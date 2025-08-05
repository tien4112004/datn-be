package com.datn.aiservice.utils;

import com.datn.aiservice.dto.request.OutlinePromptRequest;
import com.datn.aiservice.dto.request.PresentationPromptRequest;

import java.util.Map;

public class MappingParamsUtils {
    public static Map<String, Object> constructParams(OutlinePromptRequest request) {
        return Map.of(
                "language", request.getLanguage(),
                "topic", request.getTopic(),
                "slide_count", request.getSlideCount(),
                "learning_objective", request.getLearningObjective(),
                "target_age", request.getTargetAge());
    }

    public static Map<String, Object> constructParams(PresentationPromptRequest request) {
        return Map.of(
                "outline", request.getOutline(),
                "language", "vietnamese", // Default language
                "slide_count", "10",
                "target_audience", "general public", // Default target audience
                "learning_objective", "understand basic concepts", // Default learning objective
                "target_age", "7-10"); // Default target age
    }
}
