package com.datn.datnbe.document.management;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

public class MediaValidation {
    private final static long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE, "File size exceeds maximum allowed size of 10MB");
        }
    }

    public static void validateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Original filename cannot be empty");
        }
    }
}
