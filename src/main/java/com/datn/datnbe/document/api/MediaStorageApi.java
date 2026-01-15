package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.MediaMetadataDto;
import com.datn.datnbe.document.dto.response.MultiUploadedMediaResponseDto;
import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaStorageApi {
    /**
     * Upload any supported media file
     *
     * @param file the file to upload
     * @param ownerId the ID of the user who owns this media
     * @return the CDN URL of the uploaded file
     */
    UploadedMediaResponseDto upload(MultipartFile file, String ownerId);

    /**
     * Upload media file with generation metadata
     *
     * @param file the file to upload
     * @param ownerId the ID of the user who owns this media
     * @param metadata generation metadata (isGenerated, presentationId, prompt)
     * @return the uploaded media response
     */
    UploadedMediaResponseDto upload(MultipartFile file, String ownerId, MediaMetadataDto metadata);

    /**
     * Upload any supported media file
     *
     * @param file the file to upload
     * @return the CDN URL of the uploaded file
     * @deprecated Use {@link #upload(MultipartFile, String)} instead
     */
    @Deprecated
    UploadedMediaResponseDto upload(MultipartFile file);

    /**
     * Get media by ID
     *
     * @param mediaId the ID of the media to retrieve
     * @return the media object
     */
    Media getMedia(Long mediaId);

    /**
     * Delete media file and database record
     *
     * @param mediaId the ID of the media to delete
     */
    void deleteMedia(Long mediaId);

    /**
     * Upload multiple media files
     *
     * @param files the files to upload
     * @param ownerId the ID of the user who owns these media files
     * @return the uploaded media responses
     */
    MultiUploadedMediaResponseDto uploadMultiple(List<MultipartFile> files, String ownerId);

    /**
     * Upload multiple media files with generation metadata
     *
     * @param files the files to upload
     * @param ownerId the ID of the user who owns these media files
     * @param metadata generation metadata (isGenerated, presentationId, prompt)
     * @return the uploaded media responses
     */
    MultiUploadedMediaResponseDto uploadMultiple(List<MultipartFile> files, String ownerId, MediaMetadataDto metadata);
}
