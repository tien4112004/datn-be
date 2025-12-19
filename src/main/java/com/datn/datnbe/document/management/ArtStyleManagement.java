package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.ArtStyleApi;
import com.datn.datnbe.document.dto.request.ArtStyleCollectionRequest;
import com.datn.datnbe.document.dto.request.ArtStyleCreateRequest;
import com.datn.datnbe.document.dto.request.ArtStyleUpdateRequest;
import com.datn.datnbe.document.dto.response.ArtStyleResponseDto;
import com.datn.datnbe.document.entity.ArtStyle;
import com.datn.datnbe.document.management.validation.ArtStyleValidation;
import com.datn.datnbe.document.mapper.ArtStyleMapper;
import com.datn.datnbe.document.repository.ArtStyleRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.sharedkernel.service.R2StorageService;
import com.datn.datnbe.sharedkernel.utils.MediaStorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class ArtStyleManagement implements ArtStyleApi {

    private static final String ART_STYLES_FOLDER = "artStyles";

    final ArtStyleRepository artStyleRepository;
    final ArtStyleMapper artStyleMapper;
    final R2StorageService r2StorageService;

    @Value("${cloudflare.r2.public-url}")
    String cdnDomain;

    @Override
    public PaginatedResponseDto<ArtStyleResponseDto> getAllArtStyles(ArtStyleCollectionRequest request) {
        log.info("Fetching art styles - page: {}, pageSize: {}", request.getPage(), request.getPageSize());

        Pageable pageable = PageRequest
                .of(request.getPage() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ArtStyle> stylePage = artStyleRepository.findAllByIsEnabledTrue(pageable);

        List<ArtStyleResponseDto> responseDtos = stylePage.getContent().stream().map(style -> {
            try {
                return artStyleMapper.toResponseDto(style);
            } catch (Exception ex) {
                log.warn("Failed to map ArtStyle to DTO for ID {}: {}", style.getId(), ex.getMessage());
                return ArtStyleResponseDto.builder().id(style.getId()).name(style.getName()).build();
            }
        }).collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                stylePage.getTotalElements(), stylePage.getTotalPages());

        log.info("Retrieved {} art styles out of {} total", responseDtos.size(), stylePage.getTotalElements());

        return new PaginatedResponseDto<>(responseDtos, pagination);
    }

    @Override
    @Transactional
    public ArtStyleResponseDto createArtStyle(ArtStyleCreateRequest request) {
        log.info("Creating art style with id: {}", request.getId());

        if (artStyleRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Art style with ID '" + request.getId() + "' already exists");
        }

        // Validate and upload visual image if provided
        String visualCdnUrl = null;
        if (request.getVisual() != null && !request.getVisual().trim().isEmpty()) {
            ArtStyleValidation.validateBase64Image(request.getVisual());
            visualCdnUrl = uploadVisualImage(request.getVisual(), request.getId());
        }

        ArtStyle entity = artStyleMapper.toEntity(request);
        entity.setVisual(visualCdnUrl);

        ArtStyle savedEntity = artStyleRepository.save(entity);

        log.info("Created art style with id: {}", savedEntity.getId());
        return artStyleMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public ArtStyleResponseDto updateArtStyle(String id, ArtStyleUpdateRequest request) {
        log.info("Updating art style with id: {}", id);

        ArtStyle entity = artStyleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Art style not found with id: " + id));

        // Handle visual image update if provided
        if (request.getVisual() != null && !request.getVisual().trim().isEmpty()) {
            ArtStyleValidation.validateBase64Image(request.getVisual());

            // Delete old image if exists
            deleteOldVisualImage(entity.getVisual());

            // Upload new image
            String visualCdnUrl = uploadVisualImage(request.getVisual(), entity.getId());
            entity.setVisual(visualCdnUrl);
        }

        // Update other fields via mapper (excluding visual which we handle separately)
        artStyleMapper.updateEntityExcludingVisual(entity, request);

        ArtStyle savedEntity = artStyleRepository.save(entity);

        log.info("Updated art style with id: {}", savedEntity.getId());
        return artStyleMapper.toResponseDto(savedEntity);
    }

    /**
     * Uploads a base64 encoded image to R2 storage
     *
     * @param base64Image the base64 data URI string
     * @param value       the art style value (used for storage key)
     * @return the CDN URL of the uploaded image
     */
    private String uploadVisualImage(String base64Image, String id) {
        String extension = ArtStyleValidation.extractExtension(base64Image);
        String contentType = ArtStyleValidation.extractContentType(base64Image);
        String storageKey = buildStorageKey(id, extension);
        String filename = id + "." + extension;

        MultipartFile multipartFile = ArtStyleValidation.convertToMultipartFile(base64Image, filename);

        r2StorageService.uploadFile(multipartFile, storageKey, contentType);

        String cdnUrl = MediaStorageUtils.buildCdnUrl(storageKey, cdnDomain);
        log.info("Uploaded art style visual to: {}", cdnUrl);

        return cdnUrl;
    }

    /**
     * Deletes an old visual image from R2 storage
     *
     * @param cdnUrl the CDN URL of the image to delete
     */
    private void deleteOldVisualImage(String cdnUrl) {
        if (cdnUrl == null || cdnUrl.trim().isEmpty()) {
            return;
        }

        try {
            String storageKey = MediaStorageUtils.extractStorageKeyFromUrl(cdnUrl, cdnDomain);
            r2StorageService.deleteFile(storageKey);
            log.info("Deleted old art style visual: {}", storageKey);
        } catch (Exception e) {
            log.warn("Failed to delete old art style visual: {}", e.getMessage());
            // Graceful failure - don't block the update
        }
    }

    /**
     * Builds the storage key for an art style visual
     *
     * @param id        the art style id
     * @param extension the file extension
     * @return the storage key (e.g., "artStyles/photorealistic.png")
     */
    private String buildStorageKey(String id, String extension) {
        return ART_STYLES_FOLDER + "/" + id + "." + extension;
    }
}
