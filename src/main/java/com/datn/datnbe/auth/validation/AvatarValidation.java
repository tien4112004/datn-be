package com.datn.datnbe.auth.validation;

import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
public class AvatarValidation {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    /**
     * Validates avatar file for size, format, and content type
     *
     * @param file the avatar file to validate
     * @throws AppException if validation fails
     */
    public static void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Avatar file cannot be empty");
        }

        // Validate file size (5MB limit)
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE, "Avatar file size must not exceed 5MB");
        }

        // Validate filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Original filename cannot be empty");
        }

        // Validate file extension
        String extension = MediaType.getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Avatar must be a valid image file (JPG, PNG, GIF)");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Avatar must be a valid image file (JPG, PNG, GIF)");
        }

        log.debug("Avatar file validation passed for file: {}", originalFilename);
    }
}
