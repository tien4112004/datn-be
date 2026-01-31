package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.ai.entity.TokenUsage;
import com.datn.datnbe.ai.management.ImageGenerationIdempotencyService;
import com.datn.datnbe.ai.mapper.ImageGenerateMapper;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/images/generate")
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImage(@RequestBody ImagePromptRequest request) {
        log.info("Received image generation request: {}", request);
        String ownerId = securityContextUtils.getCurrentUserId();

        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request);

        log.info("uploading images to media storage");
        ImageResponseDto uploadedMedia = imageGenerateMapper
                .toImageResponseDto(imageResponse, mediaStorageApi, ownerId);
        log.info("Images uploaded successfully: {}", uploadedMedia);
        String fullPrompt = MappingParamsUtils.createPrompt(request);
        recordImageTokenUsage(ownerId, "image", request, null, fullPrompt);

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
            @RequestBody ImagePromptRequest request) {
        String ownerId = securityContextUtils.getCurrentUserId();

        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request);

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

        // Record token usage
        recordImageTokenUsage(ownerId, "image", request, request.getPresentationId(), fullPrompt);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @PostMapping("/image/generate-in-presentation/mock")
    @Idempotent(serviceType = ImageGenerationIdempotencyService.class)
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateMockImageWithIdempotency(
            @RequestBody ImagePromptRequest request) {
        log.info("Received mock image generation request with idempotency: {}", request);
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

    private void recordImageTokenUsage(String userId, String requestType, ImagePromptRequest request, String documentId, String requestBody) {
        try {
            TokenUsage tokenUsage = TokenUsage.builder()
                    .userId(userId)
                    .request(requestType)
                    .tokenCount(null)
                    .model(request.getModel())
                    .documentId(documentId)
                    .request(requestBody)
                    .provider(request.getProvider())
                    .build();
            tokenUsageApi.recordTokenUsage(tokenUsage);
        } catch (Exception e) {
            log.warn("Failed to record token usage for user: {} with type: {}", userId, requestType, e);
        }
    }
}
