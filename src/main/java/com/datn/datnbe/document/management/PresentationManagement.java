package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.PresentationCollectionRequest;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateTitleRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.management.validation.PresentationValidation;
import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.service.R2StorageService;
import com.datn.datnbe.sharedkernel.utils.MediaStorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PresentationManagement implements PresentationApi {

    PresentationRepository presentationRepository;
    PresentationEntityMapper mapper;
    PresentationValidation validation;
    ResourcePermissionApi resourcePermissionApi;
    R2StorageService r2StorageService;

    @NonFinal
    @Value("${cloudflare.r2.public-url}")
    String cdnDomain;

    private String generateUniqueTitle(String originalTitle) {
        log.info("Generating unique title for {}", originalTitle);
        if (!presentationRepository.existsByTitle(originalTitle)) {
            return originalTitle;
        }

        String candidate = originalTitle;
        int num = 0;
        while (true) {
            candidate = originalTitle + " (" + (num + 1) + ")";
            num++;

            if (num > 10000) {
                break;
            }

            if (!presentationRepository.existsByTitle(candidate)) {
                return candidate;
            }
        }

        // Fall back
        return candidate + " (" + (System.currentTimeMillis()) + ")";
    }

    @Override
    public PresentationCreateResponseDto createPresentation(PresentationCreateRequest request) {
        log.info("Creating presentation with title: {}", request.getTitle());

        // Generate unique title if duplicate exists
        String uniqueTitle = generateUniqueTitle(request.getTitle());
        if (!uniqueTitle.equals(request.getTitle())) {
            log.info("Title '{}' already exists, using '{}' instead", request.getTitle(), uniqueTitle);
        }

        Presentation presentation = mapper.createRequestToEntity(request);
        presentation.setTitle(uniqueTitle);
        presentation.getSlides()
                .stream()
                .filter(slide -> slide.getId() == null)
                .forEach(slide -> slide.setId(UUID.randomUUID().toString()));

        Presentation savedPresentation = presentationRepository.save(presentation);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication type");
        }
        String ownerId = ((Jwt) principal).getSubject();
        ResourceRegistrationRequest resourceRegistrationRequest = ResourceRegistrationRequest.builder()
                .id(savedPresentation.getId())
                .name(savedPresentation.getTitle())
                .resourceType("presentation")
                .build();
        resourcePermissionApi.registerResource(resourceRegistrationRequest, ownerId);

        log.info("Presentation saved with ID: {}", savedPresentation.getId());
        return mapper.toResponseDto(savedPresentation);
    }

    @Override
    public PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(
            PresentationCollectionRequest request) {
        log.info("Fetching presentations with collection request - page: {}, pageSize: {}, filter: {}, sort: {}",
                request.getPage(),
                request.getPageSize(),
                request.getFilter(),
                request.getSort());

        // Create sort object
        Sort sortOrder = "desc".equalsIgnoreCase(request.getSort())
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");

        // Create pageable object
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sortOrder);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication type");
        }
        String ownerId = ((Jwt) principal).getSubject();
        List<String> resourceIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "presentation");

        // Fetch data based on filter
        Page<Presentation> presentationPage;
        presentationPage = presentationRepository.findByIdInWithOptionalTitle(resourceIds,
                StringUtils.hasText(request.getFilter()) ? request.getFilter() : "",
                pageable);

        // Map to DTOs
        List<PresentationListResponseDto> presentations = presentationPage.getContent()
                .stream()
                .map(mapper::toListResponseDto)
                .collect(Collectors.toList());

        // Create pagination metadata
        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                presentationPage.getTotalElements(), presentationPage.getTotalPages());

        log.info("Retrieved {} presentations out of {} total",
                presentations.size(),
                presentationPage.getTotalElements());

        return new PaginatedResponseDto<>(presentations, pagination);
    }

    @Override
    public void updatePresentation(String id, PresentationUpdateRequest request, MultipartFile thumbnailFile) {
        log.info("Updating presentation with ID: {} (multipart)", id);

        Optional<Presentation> presentation = presentationRepository.findById(id);

        validation.validatePresentationExists(presentation, id);

        Presentation existingPresentation = presentation.get();

        // Process thumbnail file if provided
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            // Determine file extension and content type from uploaded file
            String contentType = thumbnailFile.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // Default to JPEG
            }

            String extension = contentType.equals("image/png") ? "png" : "jpg";

            // Build storage key with appropriate extension
            String storageKey = String.format("thumbnails/presentation/%s.%s", id, extension);

            // Upload to R2 directly
            String uploadedKey = r2StorageService.uploadFile(thumbnailFile, storageKey, contentType);

            // Build CDN URL
            String cdnUrl = MediaStorageUtils.buildCdnUrl(uploadedKey, cdnDomain);

            request.setThumbnail(cdnUrl);
            // No deletion needed - putObject overwrites existing file automatically
        }

        mapper.updateEntity(request, existingPresentation);

        Presentation savedPresentation = presentationRepository.save(existingPresentation);

        log.info("Presentation updated with ID: {}", savedPresentation.getId());
    }

    @Override
    public void updateTitlePresentation(String id, PresentationUpdateTitleRequest request) {
        log.info("Updating title of presentation with ID: {} to title: {}", id, request.getTitle());

        Optional<Presentation> presentation = presentationRepository.findById(id);

        validation.validatePresentationExists(presentation, id);

        Presentation existingPresentation = presentation.get();

        existingPresentation.setTitle(request.getTitle());
        presentationRepository.save(existingPresentation);
        log.info("Presentation title updated with ID: {}", id);
    }

    @Override
    public PresentationDto getPresentation(String id) {
        log.info("Fetching presentation with ID: {}", id);

        Optional<Presentation> presentationOpt;
        presentationOpt = presentationRepository.findById(id);
        validation.validatePresentationExists(presentationOpt, id);

        Presentation presentation = presentationOpt.get();
        log.info("Found presentation: {} with {} slides",
                presentation.getTitle(),
                presentation.getSlides() != null ? presentation.getSlides().size() : 0);

        return mapper.toDetailedDto(presentation);
    }

    @Override
    public void updatePresentationParsingStatus(String id) {
        Optional<Presentation> presentationOpt = presentationRepository.findById(id);
        validation.validatePresentationExists(presentationOpt, id);

        Presentation existingPresentation = presentationOpt.get();
        existingPresentation.setIsParsed(!existingPresentation.getIsParsed());
        presentationRepository.save(existingPresentation);
    }

    @Override
    public void deletePresentation(String id) {
        log.info("Deleting presentation with ID: {}", id);
        Optional<Presentation> presentationOpt = presentationRepository.findById(id);
        validation.validatePresentationExists(presentationOpt, id);
        Presentation presentation = presentationOpt.get();
        presentation.setDeletedAt(java.time.LocalDate.now());
        presentationRepository.save(presentation);
    }

    /**
     * Get current user ID from security context
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        // Fallback for tests
        if (principal instanceof String username) {
            return username;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication");
    }
}
