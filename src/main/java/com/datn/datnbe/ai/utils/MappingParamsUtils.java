package com.datn.datnbe.ai.utils;

import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import java.util.HashMap;
import java.util.Map;

public class MappingParamsUtils {
    public static Map<String, Object> constructParams(OutlinePromptRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", request.getLanguage());
        params.put("topic", request.getTopic());
        params.put("slide_count", request.getSlideCount());
        params.put("model", request.getModel());
        params.put("provider", request.getProvider().toLowerCase());

        Integer sanitizedGrade = PromptSanitizer.sanitizeGrade(request.getGrade());
        String sanitizedSubject = PromptSanitizer.sanitizeSubject(request.getSubject());
        if (sanitizedGrade != null) {
            params.put("grade", sanitizedGrade);
        }
        if (sanitizedSubject != null && !sanitizedSubject.isEmpty()) {
            params.put("subject", sanitizedSubject);
        }
        return params;
    }

    public static Map<String, Object> constructParams(PresentationPromptRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("outline", request.getOutline());
        params.put("model", request.getModel());
        params.put("provider", request.getProvider().toLowerCase());
        params.put("language", request.getLanguage());
        params.put("slide_count", request.getSlideCount());

        Integer sanitizedGrade = PromptSanitizer.sanitizeGrade(request.getGrade());
        String sanitizedSubject = PromptSanitizer.sanitizeSubject(request.getSubject());
        if (sanitizedGrade != null) {
            params.put("grade", sanitizedGrade);
        }
        if (sanitizedSubject != null && !sanitizedSubject.isEmpty()) {
            params.put("subject", sanitizedSubject);
        }
        return params;
    }

    public static Map<String, Object> constructParams(ImagePromptRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("prompt", createPrompt(request));
        params.put("model", request.getModel().toLowerCase());
        params.put("provider", request.getProvider().toLowerCase());
        params.put("aspect_ratio", request.getAspectRatio());
        return params;
    }

    public static Map<String, Object> constructParams(MindmapPromptRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("topic", request.getTopic());
        params.put("language", request.getLanguage());
        params.put("maxDepth", request.getMaxDepth());
        params.put("maxBranchesPerNode", request.getMaxBranchesPerNode());
        params.put("model", request.getModel());
        params.put("provider", request.getProvider().toLowerCase());

        Integer sanitizedGrade = PromptSanitizer.sanitizeGrade(request.getGrade());
        String sanitizedSubject = PromptSanitizer.sanitizeSubject(request.getSubject());
        if (sanitizedGrade != null) {
            params.put("grade", sanitizedGrade);
        }
        if (sanitizedSubject != null && !sanitizedSubject.isEmpty()) {
            params.put("subject", sanitizedSubject);
        }
        return params;
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
