package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.MultiUploadedMediaResponseDto;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MediaController {
    MediaStorageApi mediaStorageApi;
    SecurityContextUtils securityContextUtils;

    @PostMapping("/upload")
    public ResponseEntity<AppResponseDto<UploadedMediaResponseDto>> upload(MultipartFile file) {
        String currentUserId = securityContextUtils.getCurrentUserId();
        UploadedMediaResponseDto response = mediaStorageApi.upload(file, currentUserId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<AppResponseDto<MultiUploadedMediaResponseDto>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files) {
        String currentUserId = securityContextUtils.getCurrentUserId();
        MultiUploadedMediaResponseDto response = mediaStorageApi.uploadMultiple(files, currentUserId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<AppResponseDto<Media>> getMedia(@PathVariable Long mediaId) {
        Media media = mediaStorageApi.getMedia(mediaId);
        return ResponseEntity.ok(AppResponseDto.success(media));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<AppResponseDto<Void>> deleteMedia(@PathVariable Long mediaId) {
        mediaStorageApi.deleteMedia(mediaId);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
