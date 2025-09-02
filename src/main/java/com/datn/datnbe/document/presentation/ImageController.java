package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.ImageApi;
import com.datn.datnbe.document.dto.response.UploadedImageResponseDto;
import com.datn.datnbe.document.management.ImageManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageApi imageApi;

    @PostMapping("/upload")
    public ResponseEntity<AppResponseDto<UploadedImageResponseDto>> uploadImage(MultipartFile file) {
        String key = imageApi.uploadImage(file);
        UploadedImageResponseDto response = UploadedImageResponseDto.builder()
                .imageUrl(key)
                .build();
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
