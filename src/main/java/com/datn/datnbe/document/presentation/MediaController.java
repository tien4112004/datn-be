package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MediaController {
    MediaStorageApi mediaStorageApi;

    @PostMapping("/upload")
    public ResponseEntity<AppResponseDto<UploadedMediaResponseDto>> upload(MultipartFile file) {
        // Get ownerId from authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication type");
        }
        String ownerId = ((Jwt) principal).getSubject();
        
        UploadedMediaResponseDto response = mediaStorageApi.upload(file, ownerId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<AppResponseDto<Void>> deleteMedia(@PathVariable Long mediaId) {
        mediaStorageApi.deleteMedia(mediaId);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
