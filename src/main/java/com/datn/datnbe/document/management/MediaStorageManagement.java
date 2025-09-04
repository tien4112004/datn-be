package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MediaStorageApi;
import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class MediaStorageManagement implements MediaStorageApi {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {
        // 1. Resolve filename and content type
        String original = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
        String contentType = Optional.ofNullable(file.getContentType())
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));

        // 2. Decide folder by extension
        String ext = MediaType.getFileExtension(original).toLowerCase();
        String folder = MediaType.getFolderByExtension(ext);
        if (Objects.isNull(folder)) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        // 3. Build object key
        String key = String.format("%s/%s-%s", folder, UUID.randomUUID(), original);

        // 4. Prepare S3 Put request
        PutObjectRequest req = PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();

        // 5. Perform upload
        try {
            s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return key;
    }
}
