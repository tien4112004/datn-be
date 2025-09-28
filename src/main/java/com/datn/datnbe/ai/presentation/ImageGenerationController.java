package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationController {
    private final ImageGenerationApi imageGenerationApi;

    @PostMapping("image/generate")
    @Idempotent
    public ResponseEntity<AppResponseDto<ImageGeneratedResponseDto>> generateImage(
            @RequestBody ImagePromptRequest request) {
        log.info("Received image generation request: {}", request);
        List<MultipartFile> imageResponse;
        try {
            imageResponse = imageGenerationApi.generateImage(request);
            log.info("Image generation completed successfully: {}", imageResponse);
        } catch (Exception error) {
            log.error("Error generating image", error);
            throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to generate image: " + error.getMessage());
        }

        return ResponseEntity.ok(AppResponseDto.<ImageGeneratedResponseDto>builder().data(null).build());
    }

}
