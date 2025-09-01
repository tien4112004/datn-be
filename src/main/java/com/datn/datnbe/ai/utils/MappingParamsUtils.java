package com.datn.datnbe.ai.utils;


import com.datn.datnbe.ai.dto.request.ImageGenerationRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MappingParamsUtils {
    /**
     * Constructs a map of parameters for the outline generation prompt.
     *
     * @param request the OutlinePromptRequest containing the necessary fields
     * @return a map of parameters to be used in the prompt
     */
    public static Map<String, Object> constructParams(OutlinePromptRequest request) {
        return Map.of("language",
                request.getLanguage(),
                "topic",
                request.getTopic(),
                "slide_count",
                request.getSlideCount(),
                "learning_objective",
                request.getLearningObjective(),
                "target_age",
                request.getTargetAge());
    }

    /**
     * Constructs a map of parameters for the presentation slide generation prompt.
     *
     * @param request the PresentationPromptRequest containing the necessary fields
     * @return a map of parameters to be used in the prompt
     */
    public static Map<String, Object> constructParams(PresentationPromptRequest request) {
        return Map.of("outline",
                request.getOutline(),
                "language",
                request.getLanguage(),
                "slide_count",
                request.getSlideCount(),
                "learning_objective",
                request.getLearningObjective(),
                "target_age",
                request.getTargetAge());
    }

    /**
     * Constructs a map of parameters for the image generation request.
     *
     * @param request the ImageGenerationRequest containing the necessary fields
     * @return a map of parameters to be used in the image generation
     */
    public static Map<String, Object> getParamsMap(ImageGenerationRequest request) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sampleCount", request.getSampleCount());
        paramsMap.put("aspectRatio", request.getAspectRatio());
        paramsMap.put("safetyFilterLevel", request.getSafetyFilterLevel());
        paramsMap.put("personGeneration", request.getPersonGeneration());

        // Add optional parameters if provided
        if (request.getSeed() != null) {
            // paramsMap.put("seed", request.getSeed());
        } else if (request.getAddWatermark() != null) {
            paramsMap.put("addWatermark", request.getAddWatermark());
        }
        log.info("Params map for image generation: {}", paramsMap);
        return paramsMap;
    }

    /**
     * Converts a Map<String, Object> to a Protobuf Value.
     *
     * @param map the map to convert
     * @return the corresponding Protobuf Value
     * @throws InvalidProtocolBufferException if the conversion fails
     */
    public static Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        Value.Builder builder = Value.newBuilder();
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }

    /**
     * Extracts and cleans JSON content from a given response string.
     * This method removes markdown code blocks and formats the JSON appropriately.
     *
     * @param response the response string containing JSON content
     * @return a cleaned JSON string
     */
    public static String extractJsonFromResponse(String response) {
        String cleaned = response.trim();

        cleaned = cleaned.replaceAll("(?m)^```json\\s*|^```\\s*", "").replaceAll("\n---", ",").replaceAll(",\\s*$", "");

        cleaned = "{\"slides\":[" + cleaned + "]}";
        return cleaned;
    }
}
