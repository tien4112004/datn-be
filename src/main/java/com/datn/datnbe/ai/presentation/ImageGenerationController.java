package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
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

    @PostMapping("/images/generate")
    public ResponseEntity<AppResponseDto<ImageResponseDto>> generateImage(@RequestBody ImagePromptRequest request) {
        log.info("Received image generation request: {}", request);
        List<MultipartFile> imageResponse = imageGenerationApi.generateImage(request);

        log.info("uploading images to media storage");
        ImageResponseDto uploadedMedia = ImageResponseDto.builder()
                .cdnUrls(imageResponse.stream()
                        .map(mediaStorageApi::upload)
                        .map(UploadedMediaResponseDto::getCdnUrl)
                        .toList())
                .build();
        log.info("Images uploaded successfully: {}", uploadedMedia);

        return ResponseEntity.ok(AppResponseDto.<ImageResponseDto>builder().data(uploadedMedia).build());
    }

}
