package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedImageResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ImageController {
    MediaStorageApi mediaStorageApi;

    @PostMapping("/upload")
    public ResponseEntity<AppResponseDto<UploadedImageResponseDto>> upload(MultipartFile file) {
        String key = mediaStorageApi.upload(file);
        UploadedImageResponseDto response = UploadedImageResponseDto.builder().imageUrl(key).build();
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
