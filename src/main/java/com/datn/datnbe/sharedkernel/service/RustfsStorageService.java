package com.datn.datnbe.sharedkernel.service;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;

@Service
@Slf4j
public class RustfsStorageService {
    private final S3Client s3Client;

    @Value("${rustfs.bucket:default}")
    private String bucket;

    public RustfsStorageService(@Qualifier("rustfsS3Client") S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String key, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Successfully uploaded file to rustfs with key: {}", key);
            return key;
        } catch (IOException e) {
            log.error("Failed to upload file to rustfs: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to upload file");
        } catch (Exception e) {
            log.error("Failed to upload file to rustfs (non-IO error): {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to upload file");
        }
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
            log.info("Successfully deleted file from rustfs with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from rustfs: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to delete file");
        }
    }

    public GetObjectRequest getObject(String key) {
        return GetObjectRequest.builder().bucket(bucket).key(key).build();
    }
}
