package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.utils.Base64MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ArtStyleValidation {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final Pattern BASE64_DATA_URI_PATTERN = Pattern.compile("^data:image/([a-zA-Z0-9]+);base64,(.+)$");
    private static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5MB

    private static long artStyleImageSizeLimit = DEFAULT_MAX_SIZE;

    @Value("${app.media.art-style-size-limit:5242880}")
    public void setArtStyleImageSizeLimit(long limit) {
        artStyleImageSizeLimit = limit;
    }

    /**
     * Validates a base64 encoded image string
     *
     * @param base64Image the base64 data URI string (e.g., "data:image/png;base64,...")
     * @throws AppException if validation fails
     */
    public static void validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return; // Optional field, null is allowed
        }

        Matcher matcher = BASE64_DATA_URI_PATTERN.matcher(base64Image);
        if (!matcher.matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid base64 image format. Expected format: data:image/{type};base64,{data}");
        }

        String imageType = matcher.group(1).toLowerCase();
        if (!SUPPORTED_IMAGE_TYPES.contains(imageType)) {
            throw new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported image type: " + imageType + ". Supported types: " + SUPPORTED_IMAGE_TYPES);
        }

        String base64Data = matcher.group(2);
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            if (decodedBytes.length > artStyleImageSizeLimit) {
                throw new AppException(ErrorCode.FILE_TOO_LARGE,
                        "Image size exceeds maximum allowed size of " + (artStyleImageSizeLimit / 1024 / 1024) + "MB");
            }
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid base64 encoding");
        }
    }

    /**
     * Extracts the image extension from a base64 data URI
     *
     * @param base64Image the base64 data URI string
     * @return the image extension (e.g., "png", "jpg")
     */
    public static String extractExtension(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = BASE64_DATA_URI_PATTERN.matcher(base64Image);
        if (!matcher.matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid base64 image format");
        }

        return matcher.group(1).toLowerCase();
    }

    /**
     * Extracts the content type from a base64 data URI
     *
     * @param base64Image the base64 data URI string
     * @return the content type (e.g., "image/png")
     */
    public static String extractContentType(String base64Image) {
        String extension = extractExtension(base64Image);
        if (extension == null) {
            return null;
        }
        return "image/" + extension;
    }

    /**
     * Converts a base64 data URI to a MultipartFile
     *
     * @param base64Image the base64 data URI string
     * @param filename    the filename to use for the MultipartFile
     * @return a MultipartFile containing the decoded image data
     */
    public static MultipartFile convertToMultipartFile(String base64Image, String filename) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = BASE64_DATA_URI_PATTERN.matcher(base64Image);
        if (!matcher.matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid base64 image format");
        }

        String imageType = matcher.group(1).toLowerCase();
        String base64Data = matcher.group(2);
        String contentType = "image/" + imageType;

        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

        return new Base64MultipartFile(decodedBytes, filename, contentType, "visual");
    }
}
