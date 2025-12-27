package com.datn.datnbe.ai.utils;

import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import java.util.Map;

public class MappingParamsUtils {
    public static Map<String, Object> constructParams(OutlinePromptRequest request) {
        return Map.of("language",
                request.getLanguage(),
                "topic",
                request.getTopic(),
                "slide_count",
                request.getSlideCount(),
                "model",
                request.getModel(),
                "provider",
                request.getProvider().toLowerCase());
    }

    public static Map<String, Object> constructParams(PresentationPromptRequest request) {
        return Map.of("outline",
                request.getOutline(),
                "model",
                request.getModel(),
                "provider",
                request.getProvider().toLowerCase(),
                "language",
                request.getLanguage(),
                "slide_count",
                request.getSlideCount());
    }

    public static Map<String, Object> constructParams(ImagePromptRequest request) {
        return Map.of("prompt",
                createPrompt(request),
                "model",
                request.getModel().toLowerCase(),
                "provider",
                request.getProvider().toLowerCase(),
                "aspect_ratio",
                request.getAspectRatio());

    }

    public static Map<String, Object> constructParams(MindmapPromptRequest request) {
        return Map.of("topic",
                request.getTopic(),
                "language",
                request.getLanguage(),
                "maxDepth",
                request.getMaxDepth(),
                "maxBranchesPerNode",
                request.getMaxBranchesPerNode(),
                "model",
                request.getModel(),
                "provider",
                request.getProvider());
    }

    public static String createPrompt(ImagePromptRequest request) {
        StringBuilder promptBuilder = new StringBuilder(request.getPrompt());

        if (request.getArtStyle() != null && !request.getArtStyle().isEmpty()) {
            promptBuilder.append(" in the style of ").append(request.getArtStyle());
        }

        if (request.getArtDescription() != null && !request.getArtDescription().isEmpty()) {
            promptBuilder.append(", ").append(request.getArtDescription());
        }

        if (request.getThemeStyle() != null && !request.getThemeStyle().isEmpty()) {
            promptBuilder.append(", with a theme of ").append(request.getThemeStyle());
        }

        if (request.getThemeDescription() != null && !request.getThemeDescription().isEmpty()) {
            promptBuilder.append(", ").append(request.getThemeDescription());
        }

        return promptBuilder.toString();
    }
}
