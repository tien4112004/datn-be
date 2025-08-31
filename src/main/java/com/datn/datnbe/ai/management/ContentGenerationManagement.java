package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.SystemPromptConfig;
import com.datn.datnbe.ai.dto.request.ImageGenerationRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGenerationResponse;
import com.datn.datnbe.ai.factory.ChatClientFactory;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.google.cloud.vertexai.api.EndpointName;
import com.google.cloud.vertexai.api.PredictResponse;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ContentGenerationManagement implements ContentGenerationApi {
    SystemPromptConfig systemPromptConfig;
    ModelSelectionApi modelSelectionApi;
    ChatClientFactory chatClientFactory;
    PredictionServiceClient predictionServiceClient;
    EndpointName imageEndpointName;
    // AIEventPublisher aiEventPublisher;

    @Override
    public Flux<String> generateOutline(OutlinePromptRequest request) {

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for outline generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }
        var chatClient = chatClientFactory.getChatClient(request.getModel());

        return chatClient.prompt()
                .user(prompt -> prompt.text(systemPromptConfig.getOutlinePrompt())
                        .params(MappingParamsUtils.constructParams(request)))
                .stream()
                .content();
    }

    @Override
    public Flux<String> generateSlides(PresentationPromptRequest request) {
        log.info("Starting streaming presentation generation for slides");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for slide generation", request.getModel());
            return Flux.error(new AppException(ErrorCode.MODEL_NOT_ENABLED));
        }

        var chatClient = chatClientFactory.getChatClient(request.getModel());

        log.info("Calling AI to stream presentation slides");

        StringBuilder completeResponse = new StringBuilder();

        return chatClient.prompt()
                .user(promptSys -> promptSys.text(systemPromptConfig.getSlidePrompt())
                        .params(MappingParamsUtils.constructParams(request)))
                .stream()
                .content()
                .doOnNext(completeResponse::append)
                .doOnComplete(() -> {
                    log.info("Streaming presentation generation completed");
                    String cleanedJson = extractJsonFromResponse(completeResponse.toString());
                    // try {
                    // // Parse the complete JSON response
                    // PresentationResponse slideResponse = objectMapper.readValue(cleanedJson,
                    // PresentationResponse.class);
                    //
                    // // Publish event with actual slides
                    // var event = new PresentationGeneratedEvent(slideResponse.getSlides());
                    // aiEventPublisher.publishEvent(event);
                    // } catch (JsonProcessingException e) {
                    // log.error("Error parsing JSON response: {}", e.getMessage());
                    // throw new AppException(ErrorCode.JSON_PARSING_ERROR);
                    // }
                })
                .doOnError(error -> log.error("Error in streaming presentation generation: {}", error.getMessage()));
    }

    @Override
    public Mono<ImageGenerationResponse> generateImage(ImageGenerationRequest request) {
        log.info("Starting image generation with prompt: {}", request.getPrompt());

        return Mono.fromCallable(() -> {
            try {
                // Prepare instance parameters
                Map<String, Object> instancesMap = new HashMap<>();
                instancesMap.put("prompt", request.getPrompt());
                Value instances = mapToValue(instancesMap);

                // Prepare generation parameters
                Map<String, Object> paramsMap = getParamsMap(request);

                Value parameters = mapToValue(paramsMap);

                // Make prediction request
                PredictResponse predictResponse = predictionServiceClient.predict(
                        imageEndpointName, Collections.singletonList(instances), parameters);

                // Extract base64 image from response
                for (Value prediction : predictResponse.getPredictionsList()) {
                    Map<String, Value> fieldsMap = prediction.getStructValue().getFieldsMap();
                    if (fieldsMap.containsKey("bytesBase64Encoded")) {
                        String bytesBase64Encoded = fieldsMap.get("bytesBase64Encoded").getStringValue();

                        log.info("Image generation completed successfully");

                        return ImageGenerationResponse.builder()
                                .imageBase64(bytesBase64Encoded)
                                .mimeType("image/png")
                                .prompt(request.getPrompt())
                                .build();
                    }
                }

                throw new AppException(ErrorCode.GENERATION_ERROR, "No image data found in response");

            } catch (Exception e) {
                log.error("Error generating image: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate image: " + e.getMessage());
            }
        })
                .doOnSubscribe(subscription -> log.info("Starting image generation request"))
                .doOnSuccess(response -> log.info("Image generation completed successfully"))
                .doOnError(error -> log.error("Image generation failed: {}", error.getMessage()));
    }

    private static Map<String, Object> getParamsMap(ImageGenerationRequest request) {
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

    private Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        Value.Builder builder = Value.newBuilder();
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }

    private String extractJsonFromResponse(String response) {
        // Remove markdown code blocks
        String cleaned = response.trim();

        cleaned = cleaned.replaceAll("(?m)^```json\\s*|^```\\s*", "").replaceAll("\n---", ",").replaceAll(",\\s*$", "");

        cleaned = "{\"slides\":[" + cleaned + "]}";
        return cleaned;
    }
}
