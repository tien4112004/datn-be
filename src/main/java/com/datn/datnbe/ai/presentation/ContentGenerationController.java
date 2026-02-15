package com.datn.datnbe.ai.presentation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.ai.dto.response.MindmapGenerateResponseDto;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    ContentGenerationApi contentGenerationExternalApi;
    PresentationApi presentationApi;
    AIResultApi aiResultApi;
    TokenUsageApi tokenUsageApi;
    SecurityContextUtils securityContextUtils;
    PhoenixQueryService phoenixQueryService;
    static Integer OUTLINE_DELAY = 25; // milliseconds
    static Integer SLIDE_DELAY = 500; // milliseconds
    ObjectMapper objectMapper = new ObjectMapper();
    CoinPricingApi coinPricingApi;

    @PostMapping(value = "presentations/outline-generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateOutline(@RequestBody OutlinePromptRequest request) {
        log.info("Received outline generation request: {}", request);

        // Capture userId BEFORE entering reactive pipeline (on the request thread)
        String userId = securityContextUtils.getCurrentUserProfileId();
        // Generate traceId for this request to track in Phoenix
        String traceId = java.util.UUID.randomUUID().toString();
        StringBuilder result = new StringBuilder();

        // Create and return the flux with background processing
        return contentGenerationExternalApi.generateOutline(request, traceId.replace("-", ""))
                .delayElements(Duration.ofMillis(OUTLINE_DELAY))
                .doOnNext(chunk -> {
                    result.append(chunk);
                })
                .doOnError(err -> log.error("Error generating outline", err))
                .doFinally(signalType -> {
                    log.info("Outline generation completed with signal: {}", signalType);
                    if (result.length() > 0) {
                        String requestBody = null;
                        try {
                            requestBody = objectMapper.writeValueAsString(request);
                        } catch (Exception e) {
                            log.error("Failed to serialize outline request for token usage recording", e);
                        }
                        extractAndSaveTokenUsage(userId,
                                "outline",
                                traceId,
                                requestBody,
                                request.getModel(),
                                request.getProvider());
                    }
                })
                .map(chunk -> removeTokenUsageFromChunk(chunk))
                .cache();
    }

    private void extractAndSaveTokenUsage(String userId,
            String requestType,
            String traceId,
            String requestBody,
            String model,
            String provider) {
        try {
            TokenUsageInfoDto tokenUsageInfo = phoenixQueryService.getTokenUsageFromPhoenix(traceId.replace("-", ""),
                    requestType);

            if (tokenUsageInfo != null) {
                tokenUsageInfo.setModel(model);
                tokenUsageInfo.setProvider(provider);
                recordTokenUsage(userId, tokenUsageInfo, requestType, traceId, requestBody);
            } else {
                log.warn("No token usage data available from Phoenix for {} with traceId: {}", requestType, traceId);
            }
        } catch (Exception e) {
            log.warn("Failed to save token usage from Phoenix for {}", requestType, e);
        }
    }

    private String removeTokenUsageFromChunk(String chunk) {
        int tokenUsageStart = chunk.lastIndexOf("{\"token_usage\":");
        if (tokenUsageStart != -1) {
            return chunk.substring(0, tokenUsageStart).trim();
        }
        return chunk;
    }

    private String removeTokenUsageFromString(String content) {
        int tokenUsageStart = content.lastIndexOf("{\"token_usage\":");
        if (tokenUsageStart != -1) {
            return content.substring(0, tokenUsageStart).trim();
        }
        return content;
    }

    @PostMapping(value = "presentations/outline-generate/batch", produces = "application/json")
    public ResponseEntity<AppResponseDto<JsonNode>> generateOutlineBatch(@RequestBody OutlinePromptRequest request) {
        log.info("Received batch outline generation request: {}", request);
        String traceId = java.util.UUID.randomUUID().toString();
        String result;

        try {
            result = contentGenerationExternalApi.generateOutlineBatch(request, traceId.replace("-", ""));
            String userId = securityContextUtils.getCurrentUserProfileId();
            extractAndSaveTokenUsage(userId,
                    "outline",
                    traceId,
                    objectMapper.writeValueAsString(request),
                    request.getModel(),
                    request.getProvider());
            log.info("Batch outline generation completed successfully");
        } catch (Exception error) {
            log.error("Error generating outline in batch mode", error);
            throw new AppException(ErrorCode.GENERATION_ERROR,
                    "Failed to generate outline in batch mode: " + error.getMessage());
        }

        return ResponseEntity.ok().body(AppResponseDto.success(result));
    }

    @PostMapping(value = "presentations/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Flux<String>> generateSlides(@RequestBody PresentationPromptRequest request) {

        String presentationId = request.getPresentationId();
        String userId = securityContextUtils.getCurrentUserId();

        // Serialize generation options to JSON
        String generationOptionsJson = null;
        if (request.getGenerationOptions() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                generationOptionsJson = mapper.writeValueAsString(request.getGenerationOptions());
                log.info("Generation options for presentation {}: {}", presentationId, generationOptionsJson);
            } catch (Exception e) {
                log.error("Failed to serialize generation options", e);
                // Continue without options rather than failing
            }
        }

        final String optionsJson = generationOptionsJson; // Make effectively final for lambda

        // Use StringBuffer for thread safety, or AtomicReference<StringBuilder>
        StringBuffer result = new StringBuffer();

        Flux<String> slideSse = contentGenerationExternalApi
                .generateSlides(request, request.getPresentationId().replace("-", ""))
                .doOnNext(response -> log.info("Received response chunk: {}", response))
                .map(slide -> slide.substring("data: ".length()) + "\n\n")
                .delayElements(Duration.ofMillis(SLIDE_DELAY))
                .doOnNext(result::append)
                .doOnError(err -> log.error("Error generating slides for ID: {}", presentationId, err))
                .onErrorResume(err -> Flux.error(
                        new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate slides: " + err.getMessage())))
                .doOnSubscribe(s -> log.info("Client subscribed to slide generation stream for ID: {}", presentationId))
                .map(slide -> removeTokenUsageFromChunk(slide))
                .publish()
                .autoConnect(0); // Start immediately, never cancel due to subscriber count

        // Subscribe internally to guarantee completion and saving
        slideSse.doFinally(signalType -> {
            if (result.length() > 0) {
                try {
                    String cleanedResult = result.toString();
                    // Extract and save token usage
                    extractAndSaveTokenUsage(userId,
                            "presentation",
                            presentationId,
                            objectMapper.writeValueAsString(request),
                            request.getModel(),
                            request.getProvider());
                    // Remove token usage from result before saving
                    cleanedResult = removeTokenUsageFromString(cleanedResult);

                    aiResultApi.saveAIResult(cleanedResult, presentationId, optionsJson);
                    log.info("Slide generation completed with signal {}, saved {} bytes for ID: {}",
                            signalType,
                            cleanedResult.length(),
                            presentationId);
                } catch (Exception e) {
                    log.error("Failed to save AI result for presentation ID: {}", presentationId, e);
                }
            }
        }).subscribe(item -> {
        }, err -> log.error("Internal subscriber error for ID: {}", presentationId, err));

        return ResponseEntity.ok().header("X-Presentation", presentationId).body(slideSse);
    }

    @PostMapping(value = "presentations/generate/batch", produces = "application/json")
    public ResponseEntity<AppResponseDto<JsonNode>> generateSlidesBatch(
            @RequestBody PresentationPromptRequest request) {
        log.info("Received batch slide generation request: {}", request);
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "");
        String result;

        try {
            result = contentGenerationExternalApi.generateSlidesBatch(request, traceId);

            log.info("Batch slide generation completed successfully");

        } catch (Exception error) {
            log.error("Error generating slides in batch mode", error);
            throw new AppException(ErrorCode.GENERATION_ERROR,
                    "Failed to generate slides in batch mode: " + error.getMessage());
        }
        String presentationId = UUID.randomUUID().toString().replace("-", "");

        // Serialize generation options to JSON
        String generationOptionsJson = null;
        if (request.getGenerationOptions() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                generationOptionsJson = mapper.writeValueAsString(request.getGenerationOptions());
            } catch (Exception e) {
                log.error("Failed to serialize generation options", e);
            }
        }

        aiResultApi.saveAIResult(result, presentationId, generationOptionsJson);

        String title = (request.getTopic() != null && !request.getTopic().trim().isEmpty())
                ? request.getTopic()
                : "AI Generated Presentation";

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .id(presentationId)
                .title(title)
                .slides(new ArrayList<>())
                .isParsed(false)
                .build();
        var newPresentation = presentationApi.createPresentation(createRequest);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("aiResult", result);
        data.set("presentation", mapper.valueToTree(newPresentation));

        return ResponseEntity.ok().header("X-Presentation", presentationId).body(AppResponseDto.success(result));
    }

    @PostMapping(value = "mindmaps/generate", produces = "application/json")
    public ResponseEntity<AppResponseDto<MindmapGenerateResponseDto>> generateMindmap(
            @RequestBody MindmapPromptRequest request) {
        log.info("Received mindmap generation request: {}", request);
        String traceId = java.util.UUID.randomUUID().toString();

        try {
            String result = contentGenerationExternalApi.generateMindmap(request, traceId.replace("-", ""))
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            log.info("Raw mindmap generation result: {}", result);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(result);

            // Extract mindmap content and children
            JsonNode dataNode = rootNode;
            if (rootNode.has("data")) {
                dataNode = rootNode.get("data");
                // If data is a string (JSON string), parse it
                if (dataNode.isTextual()) {
                    dataNode = mapper.readTree(dataNode.asText());
                }
            }

            // Convert to MindmapGenerateResponseDto
            MindmapGenerateResponseDto mindmapDto = mapper.treeToValue(dataNode, MindmapGenerateResponseDto.class);

            // Extract and save token usage asynchronously AFTER response is sent
            recordTokenUsageAsync(securityContextUtils.getCurrentUserProfileId(),
                    "mindmap",
                    traceId,
                    mapper.writeValueAsString(request),
                    request.getModel(),
                    request.getProvider());

            return ResponseEntity.ok().body(AppResponseDto.success(mindmapDto));
        } catch (Exception error) {
            log.error("Error generating mindmap", error);
            throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate mindmap: " + error.getMessage());
        }
    }

    @Async
    protected void recordTokenUsage(String userId,
            TokenUsageInfoDto tokenUsageInfo,
            String requestType,
            String documentId,
            String requestBody) {
        try {
            Long totalTokens = tokenUsageInfo.getTotalTokens();

            if (totalTokens != null) {
                Long PriceInCoinOfRequest = coinPricingApi.getTokenPriceInCoins(tokenUsageInfo.getModel(),
                        tokenUsageInfo.getProvider(),
                        requestType.toUpperCase());
                TokenUsage tokenUsage = TokenUsage.builder()
                        .userId(userId)
                        .request(requestType)
                        .inputTokens(tokenUsageInfo.getInputTokens())
                        .outputTokens(tokenUsageInfo.getOutputTokens())
                        .tokenCount(totalTokens)
                        .model(tokenUsageInfo.getModel())
                        .documentId(documentId)
                        .requestBody(requestBody)
                        .provider(tokenUsageInfo.getProvider())
                        .actualPrice(tokenUsageInfo.getTotalPrice())
                        .calculatedPrice(PriceInCoinOfRequest)
                        .build();
                tokenUsageApi.recordTokenUsage(tokenUsage);
                log.debug("Token usage saved with price: {}", tokenUsageInfo.getTotalPrice());
            }
        } catch (Exception e) {
            log.warn("Failed to record token usage for {}", requestType, e);
        }
    }

    /**
     * Async wrapper to ensure token usage recording runs AFTER response is sent
     */
    @Async
    protected void recordTokenUsageAsync(String userId,
            String requestType,
            String traceId,
            String requestBody,
            String model,
            String provider) {
        try {
            Thread.sleep(200);
            extractAndSaveTokenUsage(userId, requestType, traceId, requestBody, model, provider);
        } catch (Exception e) {
            log.warn("Failed to record token usage for {} with traceId: {}", requestType, traceId, e);
        }
    }

}
