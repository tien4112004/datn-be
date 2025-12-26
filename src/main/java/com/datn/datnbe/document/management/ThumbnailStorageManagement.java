package com.datn.datnbe.document.management;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.service.R2StorageService;
import com.datn.datnbe.sharedkernel.utils.Base64MultipartFile;
import com.datn.datnbe.sharedkernel.utils.MediaStorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * Service for processing thumbnails: converts base64 to R2 URLs transparently
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ThumbnailStorageManagement {
    R2StorageService r2StorageService;

    @NonFinal
    @Value("${cloudflare.r2.public-url}")
    String cdnDomain;

    /**
     * Process thumbnail: if base64, upload to R2 and return URL; if already URL, return as-is
     *
     * @param thumbnail  - base64 string or URL string (can be null)
     * @param entityType - "presentation" or "mindmap"
     * @param entityId   - ID of entity
     * @param ownerId    - owner ID
     * @return CDN URL (either converted from base64 or passed through), or null if input was null
     */
    @Transactional
    public String processThumbnail(String thumbnail, String entityType, String entityId, String ownerId) {
        // If null or empty, return as-is
        if (thumbnail == null || thumbnail.isEmpty()) {
            return thumbnail;
        }

        // If already a URL (not base64), return as-is
        if (!isBase64(thumbnail)) {
            log.debug("Thumbnail is already a URL for {} {}", entityType, entityId);
            return thumbnail;
        }

        // Base64 detected - upload to R2 and convert to URL
        log.info("Converting base64 thumbnail to URL for {} with ID: {}", entityType, entityId);

        try {
            // Validate base64 format
            if (!isValidBase64Thumbnail(thumbnail)) {
                log.warn("Invalid base64 thumbnail format for {} {}, storing as-is", entityType, entityId);
                return thumbnail; // Fallback: store base64 if invalid
            }

            // Convert base64 to MultipartFile
            MultipartFile thumbnailFile = convertBase64ToMultipartFile(thumbnail, entityType, entityId);

            // Generate storage key: thumbnails/{entityType}/{entityId}.png
            String storageKey = buildThumbnailStorageKey(entityType, entityId);

            // Upload to R2
            String uploadedKey = r2StorageService.uploadFile(thumbnailFile, storageKey, "image/png");

            // Build CDN URL
            String cdnUrl = MediaStorageUtils.buildCdnUrl(uploadedKey, cdnDomain);

            log.info("Successfully converted thumbnail to URL for {} {}: {}", entityType, entityId, cdnUrl);
            return cdnUrl;

        } catch (Exception e) {
            log.error("Failed to convert base64 thumbnail to URL for {} {}: {}",
                    entityType,
                    entityId,
                    e.getMessage(),
                    e);
            // Fallback: return original base64 if upload fails
            return thumbnail;
        }
    }

    /**
     * Delete old thumbnail from R2 if it exists and is a URL
     *
     * @param thumbnailUrl - URL of the thumbnail to delete (can be null or base64)
     */
    @Transactional
    public void deleteOldThumbnail(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty() || isBase64(thumbnailUrl)) {
            log.debug("Skipping delete for null, empty, or base64 thumbnail");
            return; // Skip if null, empty, or base64
        }

        try {
            String storageKey = MediaStorageUtils.extractStorageKeyFromUrl(thumbnailUrl, cdnDomain);
            log.info("Deleting old thumbnail with storage key: {}", storageKey);

            r2StorageService.deleteFile(storageKey);
        } catch (Exception e) {
            log.warn("Failed to delete old thumbnail: {}", e.getMessage());
            // Non-critical, continue
        }
    }

    /**
     * Check if string is base64 format (starts with data:image)
     */
    private boolean isBase64(String data) {
        return data != null && data.startsWith("data:image");
    }

    /**
     * Validate base64 thumbnail format
     */
    private boolean isValidBase64Thumbnail(String base64Data) {
        return base64Data != null && base64Data.startsWith("data:image/png;base64,") && base64Data.length() > 100; // Sanity check
    }

    /**
     * Convert base64 string to MultipartFile
     */
    private MultipartFile convertBase64ToMultipartFile(String base64Data, String entityType, String entityId) {
        try {
            // Remove data:image/png;base64, prefix
            String base64 = base64Data.substring(base64Data.indexOf(",") + 1);
            byte[] decodedBytes = Base64.getDecoder().decode(base64);

            String fileName = String.format("%s-%s-thumbnail.png", entityType, entityId);

            return new Base64MultipartFile(decodedBytes, fileName, "image/png", "thumbnail");
        } catch (Exception e) {
            log.error("Failed to convert base64 to MultipartFile: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_BASE64_FORMAT, "Failed to process thumbnail data");
        }
    }

    /**
     * Build storage key for thumbnail
     */
    private String buildThumbnailStorageKey(String entityType, String entityId) {
        return String.format("thumbnails/%s/%s.png", entityType, entityId);
    }
}
