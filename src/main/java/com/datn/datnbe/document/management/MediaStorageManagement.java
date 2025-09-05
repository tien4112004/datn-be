package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.document.service.R2StorageService;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.datn.datnbe.document.utils.MediaStorageUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MediaStorageManagement implements MediaStorageApi {
    R2StorageService r2StorageService;
    MediaRepository mediaRepository;

    @NonFinal
    @Value("${cloudflare.r2.public-url}")
    String cdnDomain;

    @Override
    @Transactional
    public UploadedMediaResponseDto upload(MultipartFile file) {
        MediaValidation.validateFile(file);

        String originalFilename = getOriginalFilename(file);
        String contentType = getContentType(file);
        String extension = MediaType.getFileExtension(originalFilename);

        MediaType mediaType = MediaType.getByExtension(extension)
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                        "Unsupported file extension: " + extension));

        String sanitizedFilename = sanitizeFilename(originalFilename);
        String storageKey = buildObjectKey(mediaType.getFolder(), sanitizedFilename);

        String uploadedKey = r2StorageService.uploadFile(file, storageKey, contentType);

        Media media = Media.builder()
                .originalFilename(originalFilename)
                .storageKey(uploadedKey)
                .cdnUrl(buildCdnUrl(uploadedKey, cdnDomain))
                .mediaType(mediaType)
                .fileSize(file.getSize())
                .contentType(contentType)
                .build();

        Media savedMedia = mediaRepository.save(media);
        log.info("Saved media record with ID: {} for file: {}", savedMedia.getId(), originalFilename);

        return UploadedMediaResponseDto.builder()
                .mediaType(mediaType.name())
                .cdnUrl(savedMedia.getCdnUrl())
                .extension(extension)
                .build();
    }

    /**
     * Delete media file and database record
     */
    @Override
    @Transactional
    public void deleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND, "Media not found with ID: " + mediaId));

        // Delete from R2 storage
        r2StorageService.deleteFile(media.getStorageKey());

        // Delete from database
        mediaRepository.delete(media);
        log.info("Successfully deleted media with ID: {}", mediaId);
    }
}
