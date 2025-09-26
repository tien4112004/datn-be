package com.datn.datnbe.ai.utils;

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
                request.getProvider());
    }

    public static Map<String, Object> constructParams(PresentationPromptRequest request) {
        return Map.of("outline",
                request.getOutline(),
                "model",
                request.getModel(),
                "provider",
                request.getProvider(),
                "language",
                request.getLanguage(),
                "slide_count",
                request.getSlideCount());
    }
}
