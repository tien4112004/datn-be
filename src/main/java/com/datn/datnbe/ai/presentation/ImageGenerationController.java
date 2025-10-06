package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.ai.management.ImageGenerationIdempotencyService;
import com.datn.datnbe.ai.mapper.ImageGenerateMapper;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ImageGenerationController {
    private final ImageGenerationApi imageGenerationApi;
    private final MediaStorageApi mediaStorageApi;
    private final PresentationApi presentationApi;
    private final ImageGenerateMapper imageGenerateMapper;

    @PostMapping("/images/generate")
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImage(@RequestBody ImagePromptRequest request) {
        log.info("Received image generation request: {}", request);
        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request);

        log.info("uploading images to media storage");
        ImageResponseDto uploadedMedia = imageGenerateMapper.toImageResponseDto(imageResponse, mediaStorageApi);
        log.info("Images uploaded successfully: {}", uploadedMedia);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

    @PostMapping("/images/generate-with-idempotency")
    @Idempotent(serviceType = ImageGenerationIdempotencyService.class)
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImageWithIdempotency(
            @RequestBody ImagePromptRequest request,
            HttpServletRequest httpRequest) {

        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request);

        log.info("uploading images to media storage");
        ImageResponseDto uploadedMedia = imageGenerateMapper.toImageResponseDto(imageResponse, mediaStorageApi);
        log.info("Images uploaded successfully: {}", uploadedMedia);

        if (uploadedMedia == null) {
            throw new AppException(ErrorCode.GENERATION_ERROR, "No images were generated");
        }

        log.info("Images generated and uploaded successfully: {}", uploadedMedia);

        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");

        List<String> keys = Arrays.stream(idempotencyKey.split(":")).toList();
        String presentationId = keys.getFirst();
        String slideId = keys.get(1);
        String elementId = keys.get(2);
        // String url = uploadedMedia.getFirst().get("cdnUrl").toString();

        // Using a random image from picsum.photos as a placeholder
        int randomNumber = (int) (Math.random() * 1000);
        String url = "https://picsum.photos/800/600?random=" + randomNumber;

        if (!(presentationApi.insertImageToPresentation(presentationId, slideId, elementId, url) > 0)) {
            throw new AppException(ErrorCode.IMAGE_INSERTION_FAILED);
        }

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

}
