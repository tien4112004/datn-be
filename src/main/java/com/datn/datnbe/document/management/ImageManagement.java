package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.ImageApi;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageManagement implements ImageApi {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. Resolve filename and content type
        String original = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
        String contentType = Optional.ofNullable(file.getContentType())
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));

        // 2. Decide folder by extension
        String ext = getFileExtension(original);
        String folder = switch (ext) {
            case "jpg","jpeg","png","gif" -> "images";
            case "mp4","mov"              -> "videos";
            case "pdf","doc","docx","txt" -> "documents";
            default -> throw  new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        };

        // 3. Build object key
        String key = String.format("%s/%s-%s", folder, UUID.randomUUID(), original);

        // 4. Prepare S3 Put request
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        // 5. Perform upload
        try {
            s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return key;
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length()-1) {
            throw new IllegalArgumentException("Invalid file extension in filename: " + filename);
        }
        return filename.substring(idx+1);
    }
}
