package com.datn.aiservice.utils;

import com.datn.aiservice.dto.request.OutlinePromptRequest;

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
}
