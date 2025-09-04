package com.datn.datnbe.document.service;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2StorageService {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    /**
     * Upload file to Cloudflare R2 storage
     *
     * @param file        the file to upload
     * @param key         the object key/path in the bucket
     * @param contentType the MIME content type
     * @return the object key of the uploaded file
     */
    public String uploadFile(MultipartFile file, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Successfully uploaded file with key: {}", key);
            return key;
        } catch (IOException e) {
            log.error("Failed to upload file to R2: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Delete file from Cloudflare R2 storage
     *
     * @param key the object key to delete
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
            log.info("Successfully deleted file with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }
}
