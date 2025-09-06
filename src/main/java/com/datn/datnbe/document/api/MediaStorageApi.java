package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageApi {
    /**
     * Upload any supported media file
     *
     * @param file the file to upload
     * @return the CDN URL of the uploaded file
     */
    UploadedMediaResponseDto upload(MultipartFile file);

    /**
     * Delete media file and database record
     *
     * @param mediaId the ID of the media to delete
     */
    void deleteMedia(Long mediaId);
}
