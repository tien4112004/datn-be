package com.datn.datnbe.auth.validation;

import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class AvatarValidation {

    @Value("${app.media.avatar-size-limit:5242880}") // Default to 5MB
    private long MAX_AVATAR_SIZE;

    /**
     * Validates avatar file for size, format, and content type
     *
     * @param file the avatar file to validate
     * @throws AppException if validation fails
     */
    public void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Avatar file cannot be empty");
        }

        // Validate file size (5MB limit)
        log.info("Validating avatar file size: {} -- {}", file.getSize(), MAX_AVATAR_SIZE);
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
        if (!MediaType.getAllowedExtensionsOf(MediaType.IMAGE).contains(extension)) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Avatar must be a valid image file (JPG, PNG, GIF)");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null
                || !MediaType.getAllowedContentTypesOf(MediaType.IMAGE).contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Avatar must be a valid image file (JPG, PNG, GIF)");
        }

        log.debug("Avatar file validation passed for file: {}", originalFilename);
    }
}
