package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class MediaValidation {
    private static long fileSizeLimit;

    @Value("${app.media.file-size-limit}")
    public void setFileSizeLimit(long limit) {
        fileSizeLimit = limit;
    }

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "File cannot be empty");
        }

        if (file.getSize() > fileSizeLimit) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE, "File size exceeds maximum allowed size");
        }
    }

    public static void validateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Original filename cannot be empty");
        }
    }

    public static void validateFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Filename is required");
        }

        String extension = MediaType.getFileExtension(filename);
        MediaType.getByExtension(extension)
                .orElseThrow(() -> new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                        "Unsupported file type: " + extension));
    }

    public static MediaType getValidatedMediaType(MultipartFile file) {
        validateFile(file);
        validateFileName(file);
        validateFileType(file);

        String extension = MediaType.getFileExtension(file.getOriginalFilename());
        return MediaType.getByExtension(extension).get();
    }
}
