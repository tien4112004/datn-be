package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.document.dto.response.MultiUploadedMediaResponseDto;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.document.management.validation.MediaValidation;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.document.service.DocumentService;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.datn.datnbe.sharedkernel.service.RustfsStorageService;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.datn.datnbe.sharedkernel.utils.MediaStorageUtils.*;

@Service
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MediaStorageManagement implements MediaStorageApi {
    RustfsStorageService rustfsStorageService;
    MediaRepository mediaRepository;
    DocumentService documentVisitService;
    SecurityContextUtils securityContextUtils;

    public MediaStorageManagement(RustfsStorageService rustfsStorageService, MediaRepository mediaRepository,
            DocumentService documentVisitService, @Lazy SecurityContextUtils securityContextUtils) {
        this.rustfsStorageService = rustfsStorageService;
        this.mediaRepository = mediaRepository;
        this.documentVisitService = documentVisitService;
        this.securityContextUtils = securityContextUtils;
    }

    @NonFinal
    @Value("${rustfs.public-url}")
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
        String uploadedKey = rustfsStorageService.uploadFile(file, storageKey, contentType);

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
        String uploadedKey = rustfsStorageService.uploadFile(file, storageKey, contentType);

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
    @Transactional(readOnly = true)
    public Media getMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND, "Media not found with ID: " + mediaId));

        // Only track visits for non-image types (audio, video, document, etc)
        if (media.getMediaType() != MediaType.IMAGE) {
            String userId = securityContextUtils.getCurrentUserId();
            if (userId != null) {
                var metadata = DocumentMetadataDto.builder()
                        .userId(userId)
                        .documentId(String.valueOf(mediaId))
                        .type(media.getMediaType().name().toLowerCase())
                        .title(media.getOriginalFilename())
                        .thumbnail(media.getCdnUrl())
                        .build();
                documentVisitService.trackDocumentVisit(metadata);
            }
        }

        log.info("Retrieved media with ID: {}", mediaId);
        return media;
    }

    @Override
    @Transactional
    public void deleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND, "Media not found with ID: " + mediaId));

        // Delete from R2 storage
        rustfsStorageService.deleteFile(media.getStorageKey());

        // Delete from database
        mediaRepository.delete(media);

        // Clean up visit records
        documentVisitService.deleteDocumentVisits(String.valueOf(mediaId));

        log.info("Successfully deleted media with ID: {}", mediaId);
    }

    /**
     * Upload multiple media files
     */
    @Override
    @Transactional
    public MultiUploadedMediaResponseDto uploadMultiple(List<MultipartFile> files, String ownerId) {
        return uploadMultiple(files, ownerId, null);
    }

    /**
     * Upload multiple media files with generation metadata
     */
    @Override
    @Transactional
    public MultiUploadedMediaResponseDto uploadMultiple(List<MultipartFile> files,
            String ownerId,
            MediaMetadataDto metadata) {
        List<UploadedMediaResponseDto> uploadedMedia = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                UploadedMediaResponseDto uploadedDto = upload(file, ownerId, metadata);
                uploadedMedia.add(uploadedDto);
                log.info("Successfully uploaded file: {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Error uploading file: {}", file.getOriginalFilename(), e);
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR,
                        "Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }

        log.info("Successfully uploaded {} media files (owner: {})", uploadedMedia.size(), ownerId);
        return MultiUploadedMediaResponseDto.builder().media(uploadedMedia).totalCount(uploadedMedia.size()).build();
    }
}
