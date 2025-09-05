package com.datn.datnbe.ai.utils;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

public class MediaStorageUtils {

    public static String getOriginalFilename(MultipartFile file) {
        return Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> !name.trim().isEmpty())
                .orElseThrow(() -> new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Original filename is required"));
    }

    public static String getContentType(MultipartFile file) {
        return Optional.ofNullable(file.getContentType())
                .filter(type -> !type.trim().isEmpty())
                .orElseThrow(
                        () -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Content type cannot be determined"));
    }

    public static String buildObjectKey(String folder, String filename) {
        return String.format("%s/%s-%s", folder, UUID.randomUUID(), filename);
    }

    public static String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static String buildCdnUrl(String storageKey, String cdnDomain) {
        return cdnDomain.endsWith("/") ? cdnDomain + storageKey : cdnDomain + "/" + storageKey;
    }
}
