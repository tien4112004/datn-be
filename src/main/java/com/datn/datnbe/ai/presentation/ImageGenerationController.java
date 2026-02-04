package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.CoinPricingApi;
import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.ai.dto.response.TokenUsageInfoDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.management.ImageGenerationIdempotencyService;
import com.datn.datnbe.ai.mapper.ImageGenerateMapper;
import com.datn.datnbe.ai.service.PhoenixQueryService;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ImageGenerationController {
    private final ImageGenerationApi imageGenerationApi;
    private final MediaStorageApi mediaStorageApi;
    private final ImageGenerateMapper imageGenerateMapper;
    private final SecurityContextUtils securityContextUtils;
    private final TokenUsageApi tokenUsageApi;
    private final PhoenixQueryService phoenixQueryService;
    private final CoinPricingApi coinPricingApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/images/generate")
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImage(@RequestBody ImagePromptRequest request) {
        log.info("Received image generation request: {}", request);
        String ownerId = securityContextUtils.getCurrentUserId();

        String traceId = java.util.UUID.randomUUID().toString();

        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request, traceId.replace("-", ""));

        log.info("uploading images to media storage");
        ImageResponseDto uploadedMedia = imageGenerateMapper
                .toImageResponseDto(imageResponse, mediaStorageApi, ownerId);
        log.info("Images uploaded successfully: {}", uploadedMedia);
        String fullRequestBody;
        try {
            fullRequestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            fullRequestBody = "";
        }
        recordImageTokenUsage(ownerId, "image", request, traceId, fullRequestBody);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @PostMapping("/image/generate/mock")
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateMockImage(@RequestBody ImagePromptRequest request) {
        log.info("Received mock image generation request: {}", request);
        String ownerId = securityContextUtils.getCurrentUserId();

        List<MultipartFile> imageResponse = imageGenerationApi.generateMockImage(request);

        // Prepare metadata
        String fullPrompt = MappingParamsUtils.createPrompt(request);
        MediaMetadataDto metadata = MediaMetadataDto.builder()
                .isGenerated(true)
                .presentationId(null)
                .prompt(fullPrompt)
                .model(request.getModel())
                .provider(request.getProvider())
                .build();

        log.info("uploading images to media storage with metadata");
        ImageResponseDto uploadedMedia = imageGenerateMapper
                .toImageResponseDtoWithMetadata(imageResponse, mediaStorageApi, ownerId, metadata);
        log.info("Images uploaded successfully: {}", uploadedMedia);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @PostMapping("/images/generate-in-presentation")
    @Idempotent(serviceType = ImageGenerationIdempotencyService.class)
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImageWithIdempotency(
            @RequestBody ImagePromptRequest request,
            HttpServletRequest httpRequest) {
        String ownerId = securityContextUtils.getCurrentUserId();
        String idempotencyKey = httpRequest.getHeader("idempotency-key");
        String presentationId = idempotencyKey.split(":")[0];
        if (request.getPresentationId() == null || request.getPresentationId().isEmpty()) {
            request.setPresentationId(presentationId);
        }
        log.info("Idempotency key: {}", idempotencyKey);

        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request,
                request.getPresentationId().replace("-", ""));

        // Prepare metadata with presentation context
        String fullPrompt = MappingParamsUtils.createPrompt(request);
        MediaMetadataDto metadata = MediaMetadataDto.builder()
                .isGenerated(true)
                .presentationId(request.getPresentationId())
                .prompt(fullPrompt)
                .model(request.getModel())
                .provider(request.getProvider())
                .build();

        log.info("uploading images to media storage with metadata (presentationId: {})", request.getPresentationId());
        ImageResponseDto uploadedMedia = imageGenerateMapper
                .toImageResponseDtoWithMetadata(imageResponse, mediaStorageApi, ownerId, metadata);
        log.info("Images uploaded successfully: {}", uploadedMedia);
        String fullRequestBody;
        try {
            fullRequestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            fullRequestBody = "";
        }

        // Record token usage
        recordImageTokenUsage(ownerId, "image", request, request.getPresentationId(), fullRequestBody);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @PostMapping("/image/generate-in-presentation/mock")
    @Idempotent(serviceType = ImageGenerationIdempotencyService.class)
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateMockImageWithIdempotency(
            @RequestBody ImagePromptRequest request,
            HttpServletRequest httpRequest) {
        String idempotencyKey = httpRequest.getHeader("idempotency-key");
        log.info("Received mock image generation request with idempotency: {}, idempotency-key: {}",
                request,
                idempotencyKey);
        String presentationId = idempotencyKey.split(":")[0];
        if (request.getPresentationId() == null || request.getPresentationId().isEmpty()) {
            request.setPresentationId(presentationId);
        }
        String ownerId = securityContextUtils.getCurrentUserId();

        List<MultipartFile> imageResponse = imageGenerationApi.generateMockImage(request);

        // Prepare metadata with presentation context
        String fullPrompt = MappingParamsUtils.createPrompt(request);
        MediaMetadataDto metadata = MediaMetadataDto.builder()
                .isGenerated(true)
                .presentationId(request.getPresentationId())
                .prompt(fullPrompt)
                .model(request.getModel())
                .provider(request.getProvider())
                .build();

        log.info("uploading images to media storage with metadata (presentationId: {})", request.getPresentationId());
        ImageResponseDto uploadedMedia = imageGenerateMapper
                .toImageResponseDtoWithMetadata(imageResponse, mediaStorageApi, ownerId, metadata);
        log.info("Images uploaded successfully: {}", uploadedMedia);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @Async
    private void recordImageTokenUsage(String userId,
            String requestType,
            ImagePromptRequest request,
            String documentId,
            String requestBody) {
        try {
            // Query Phoenix API to get token usage for image generation
            TokenUsageInfoDto tokenUsageInfo = phoenixQueryService.getTokenUsageFromPhoenix(documentId.replace("-", ""),
                    requestType);

            if (tokenUsageInfo != null) {
                Long PriceInCoinOfRequest = coinPricingApi.getTokenPriceInCoins(tokenUsageInfo.getModel(),
                        tokenUsageInfo.getProvider(),
                        requestType.toUpperCase());
                TokenUsage tokenUsage = TokenUsage.builder()
                        .userId(userId)
                        .request(requestType)
                        .inputTokens(tokenUsageInfo.getInputTokens())
                        .outputTokens(tokenUsageInfo.getOutputTokens())
                        .tokenCount(tokenUsageInfo.getTotalTokens())
                        .model(tokenUsageInfo.getModel() != null ? tokenUsageInfo.getModel() : request.getModel())
                        .documentId(documentId)
                        .requestBody(requestBody)
                        .provider(tokenUsageInfo.getProvider() != null
                                ? tokenUsageInfo.getProvider()
                                : request.getProvider())
                        .actualPrice(tokenUsageInfo.getTotalPrice())
                        .calculatedPrice(PriceInCoinOfRequest)
                        .build();
                tokenUsageApi.recordTokenUsage(tokenUsage);
                log.debug("Token usage saved from Phoenix for image generation - tokens: {}, price: {}",
                        tokenUsageInfo.getTotalTokens(),
                        tokenUsageInfo.getTotalPrice());
            } else {
                // Fallback: if Phoenix is unavailable, record with model/provider from request (without token count and price)
                TokenUsage tokenUsage = TokenUsage.builder()
                        .userId(userId)
                        .request(requestType)
                        .model(request.getModel())
                        .documentId(documentId)
                        .requestBody(requestBody)
                        .provider(request.getProvider())
                        .build();
                tokenUsageApi.recordTokenUsage(tokenUsage);
                log.warn(
                        "No token usage data from Phoenix for image generation, recorded without token count and price");
            }
        } catch (Exception e) {
            log.warn("Failed to record token usage for user: {} with type: {}", userId, requestType, e);
        }
    }
}
