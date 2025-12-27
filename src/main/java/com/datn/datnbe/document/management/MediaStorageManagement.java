package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.document.management.validation.MediaValidation;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.datn.datnbe.sharedkernel.utils.MediaStorageUtils.*;

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
    public UploadedMediaResponseDto upload(MultipartFile file, String ownerId) {
        var mediaType = MediaValidation.getValidatedMediaType(file);

        String originalFilename = getOriginalFilename(file);
        String contentType = getContentType(file);
        String extension = MediaType.getFileExtension(originalFilename);
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
                .ownerId(ownerId)
                .build();

        Media savedMedia = mediaRepository.save(media);
        log.info("Saved media record with ID: {} for file: {} (owner: {})",
                savedMedia.getId(),
                originalFilename,
                ownerId);

        return UploadedMediaResponseDto.builder()
                .id(savedMedia.getId())
                .mediaType(mediaType.name())
                .cdnUrl(savedMedia.getCdnUrl())
                .extension(extension)
                .build();
    }

    @Override
    @Transactional
    public UploadedMediaResponseDto upload(MultipartFile file, String ownerId, MediaMetadataDto metadata) {
        var mediaType = MediaValidation.getValidatedMediaType(file);

        String originalFilename = getOriginalFilename(file);
        String contentType = getContentType(file);
        String extension = MediaType.getFileExtension(originalFilename);
        String sanitizedFilename = sanitizeFilename(originalFilename);

        String storageKey = buildObjectKey(mediaType.getFolder(), sanitizedFilename);
        String uploadedKey = r2StorageService.uploadFile(file, storageKey, contentType);

        Media.MediaBuilder mediaBuilder = Media.builder()
                .originalFilename(originalFilename)
                .storageKey(uploadedKey)
                .cdnUrl(buildCdnUrl(uploadedKey, cdnDomain))
                .mediaType(mediaType)
                .fileSize(file.getSize())
                .contentType(contentType)
                .ownerId(ownerId);

        // Add metadata if provided
        if (metadata != null) {
            if (metadata.getIsGenerated() != null) {
                mediaBuilder.isGenerated(metadata.getIsGenerated());
            }
            if (metadata.getPresentationId() != null) {
                mediaBuilder.presentationId(metadata.getPresentationId());
            }
            if (metadata.getPrompt() != null) {
                mediaBuilder.prompt(metadata.getPrompt());
            }
            if (metadata.getModel() != null) {
                mediaBuilder.model(metadata.getModel());
            }
            if (metadata.getProvider() != null) {
                mediaBuilder.provider(metadata.getProvider());
            }
        }

        Media media = mediaBuilder.build();
        Media savedMedia = mediaRepository.save(media);

        log.info("Saved media record with ID: {} for file: {} (owner: {}, generated: {}, presentationId: {})",
                savedMedia.getId(),
                originalFilename,
                ownerId,
                savedMedia.getIsGenerated(),
                savedMedia.getPresentationId());

        return UploadedMediaResponseDto.builder()
                .id(savedMedia.getId())
                .mediaType(mediaType.name())
                .cdnUrl(savedMedia.getCdnUrl())
                .extension(extension)
                .build();
    }

    @Override
    @Transactional
    @Deprecated
    public UploadedMediaResponseDto upload(MultipartFile file) {
        return upload(file, null);
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
