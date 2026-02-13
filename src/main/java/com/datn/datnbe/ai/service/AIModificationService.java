package com.datn.datnbe.ai.service;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.AIModificationResponse; // Use the one we created or move it
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.requests.*;
import com.datn.datnbe.ai.dto.response.ImageGenerateResponse;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.datn.datnbe.sharedkernel.utils.Base64MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIModificationService {
    @Value("${ai.api.image-endpoint}")
    @NonFinal
    String IMAGE_API_ENDPOINT;

    private final AIApiClient aiApiClient;
    private final MediaStorageApi mediaStorageApi;
    private final SecurityContextUtils securityContextUtils;

    // These endpoints must match the AI Worker's router
    private static final String AI_REFINE_ENDPOINT = "/api/modification/refine";
    private static final String AI_LAYOUT_ENDPOINT = "/api/modification/layout";
    private static final String AI_REFINE_TEXT_ENDPOINT = "/api/modification/refine-text";
    private static final String AI_REFINE_COMBINED_TEXT_ENDPOINT = "/api/modification/refine-combined-text";

    public AIModificationResponse refineContent(RefineContentRequest request) {
        log.info("Refining content for slide: {}", request.getContext().getSlideId());
        setDefaultModelAndProvider(request);
        // Delegate to worker
        return aiApiClient.post(AI_REFINE_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse transformLayout(TransformLayoutRequest request) {
        log.info("Transforming layout to: {}", request.getTargetType());
        setDefaultModelAndProvider(request);
        return aiApiClient.post(AI_LAYOUT_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse refineElementText(RefineElementTextRequest request) {
        log.info("Refining text for element: {}", request.getElementId());
        setDefaultModelAndProvider(request);
        return aiApiClient.post(AI_REFINE_TEXT_ENDPOINT, request, AIModificationResponse.class);
    }

    public AIModificationResponse replaceElementImage(ReplaceElementImageRequest request) {
        log.info("Replacing image for element: {}", request.getElementId());
        setDefaultModelAndProvider(request);

        try {
            // Step 1: Build complete prompt in Spring Boot (business logic)
            // Map ReplaceElementImageRequest to ImagePromptRequest to use existing utility
            ImagePromptRequest imagePromptRequest = new ImagePromptRequest();
            imagePromptRequest.setPrompt(request.getDescription());
            imagePromptRequest.setArtStyle(request.getStyle());
            imagePromptRequest.setArtDescription(request.getArtDescription());
            imagePromptRequest.setThemeDescription(request.getThemeDescription());
            String completePrompt = MappingParamsUtils.createPrompt(imagePromptRequest);

            // Step 2: Create request for generic image generation endpoint
            ImageGenerateRequest imageRequest = new ImageGenerateRequest();
            imageRequest.setPrompt(completePrompt);
            imageRequest.setModel("gemini-2.5-flash-image");  // Hardcoded for image generation
            imageRequest.setProvider("google");
            imageRequest.setNumberOfImages(1);
            imageRequest.setAspectRatio("1:1");
            imageRequest.setSafetyFilterLevel("block_few");
            imageRequest.setPersonGeneration("allow_all");
            imageRequest.setNegativePrompt("text, watermark");

            // Step 3: Call ai-worker generic image generation endpoint (lightweight)
            ImageGenerateResponse workerResponse = aiApiClient
                    .post(IMAGE_API_ENDPOINT, imageRequest, ImageGenerateResponse.class);

            // Step 4: Extract base64 image data
            if (workerResponse == null || workerResponse.getImages() == null || workerResponse.getImages().isEmpty()) {
                log.error("No image generated from AI worker");
                return AIModificationResponse.error("No image generated from AI worker");
            }

            String base64Data = workerResponse.getImages().get(0);
            String base64DataUri = "data:image/png;base64," + base64Data;

            // Step 5: Convert base64 to MultipartFile
            MultipartFile imageFile = convertBase64DataUriToMultipartFile(base64DataUri);

            // Step 6: Prepare metadata for tracking
            String ownerId = securityContextUtils.getCurrentUserId();
            MediaMetadataDto metadata = MediaMetadataDto.builder()
                    .isGenerated(true)
                    .presentationId(request.getSlideId())
                    .prompt(completePrompt)
                    .model("gemini-2.5-flash-image")
                    .provider("google")
                    .build();

            // Step 7: Upload to MediaStorage and get CDN URL
            UploadedMediaResponseDto uploadedMedia = mediaStorageApi.upload(imageFile, ownerId, metadata);
            log.info("Uploaded generated image to CDN: {}", uploadedMedia.getCdnUrl());

            // Step 8: Return CDN URL in same format
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("imageUrl", uploadedMedia.getCdnUrl());
            return AIModificationResponse.success(resultData);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input for image generation: {}", e.getMessage());
            return AIModificationResponse.error("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to replace element image", e);
            return AIModificationResponse.error("Failed to replace image: " + e.getMessage());
        }
    }

    /**
     * Convert base64 data URI to MultipartFile
     * Extracts base64 data from format: "data:image/png;base64,{data}"
     */
    private MultipartFile convertBase64DataUriToMultipartFile(String base64DataUri) {
        try {
            // Extract base64 data from data URI
            String[] parts = base64DataUri.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid base64 data URI format");
            }

            String base64Data = parts[1];
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            // Detect content type from data URI
            String contentType = "image/png";
            if (parts[0].contains("image/jpeg") || parts[0].contains("image/jpg")) {
                contentType = "image/jpeg";
            } else if (parts[0].contains("image/webp")) {
                contentType = "image/webp";
            }

            String extension = contentType.equals("image/jpeg") ? "jpg" : "png";
            return new Base64MultipartFile(decoded, "AI_replaced_image." + extension, contentType, "image");

        } catch (IllegalArgumentException e) {
            log.error("Error decoding base64 image", e);
            throw e;
        } catch (Exception e) {
            log.error("Error converting base64 to MultipartFile", e);
            throw new RuntimeException("Failed to convert base64 image", e);
        }
    }

    public AIModificationResponse refineCombinedText(ExpandCombinedTextRequest request) {
        log.info("Refining combined text for slide: {}", request.getSlideId());
        setDefaultModelAndProvider(request);
        return aiApiClient.post(AI_REFINE_COMBINED_TEXT_ENDPOINT, request, AIModificationResponse.class);
    }

    /**
     * Build full prompt from request for metadata tracking
     */
    private String buildPromptFromRequest(ReplaceElementImageRequest request) {
        StringBuilder prompt = new StringBuilder(request.getDescription());
        if (request.getStyle() != null && !request.getStyle().isEmpty()) {
            prompt.append(" in ").append(request.getStyle()).append(" style");
        }
        if (request.getArtDescription() != null && !request.getArtDescription().isEmpty()) {
            prompt.append(", ").append(request.getArtDescription());
        }
        if (request.getThemeDescription() != null && !request.getThemeDescription().isEmpty()) {
            prompt.append(", ").append(request.getThemeDescription());
        }
        return prompt.toString();
    }

    /**
     * Set default model and provider if they are null.
     * This provides backward compatibility for clients that don't yet send these fields.
     */
    private void setDefaultModelAndProvider(Object request) {
        if (request instanceof RefineContentRequest) {
            RefineContentRequest req = (RefineContentRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof TransformLayoutRequest) {
            TransformLayoutRequest req = (TransformLayoutRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof RefineElementTextRequest) {
            RefineElementTextRequest req = (RefineElementTextRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof ReplaceElementImageRequest) {
            ReplaceElementImageRequest req = (ReplaceElementImageRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        } else if (request instanceof ExpandCombinedTextRequest) {
            ExpandCombinedTextRequest req = (ExpandCombinedTextRequest) request;
            if (req.getModel() == null) {
                req.setModel("gemini-2.0-flash-exp");
            }
            if (req.getProvider() == null) {
                req.setProvider("google");
            }
        }
    }
}
