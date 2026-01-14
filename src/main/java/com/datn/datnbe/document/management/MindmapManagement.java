package com.datn.datnbe.document.management;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.document.api.MindmapApi;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleAndDescriptionRequest;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.document.entity.Mindmap;
import com.datn.datnbe.document.management.validation.MindmapValidation;
import com.datn.datnbe.document.mapper.MindmapEntityMapper;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.document.service.DocumentVisitService;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.sharedkernel.service.RustfsStorageService;
import com.datn.datnbe.sharedkernel.utils.MediaStorageUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MindmapManagement implements MindmapApi {

    MindmapRepository mindmapRepository;
    MindmapEntityMapper mapper;
    MindmapValidation validation;
    ResourcePermissionApi resourcePermissionApi;
    RustfsStorageService rustfsStorageService;
    DocumentVisitService documentVisitService;

    @NonFinal
    @Value("${rustfs.public-url}")
    String cdnDomain;

    @Override
    public MindmapCreateResponseDto createMindmap(MindmapCreateRequest request) {
        log.info("Creating mindmap with title: '{}'", request.getTitle());

        try {
            Mindmap mindmap = buildMindmapFromRequest(request);
            Mindmap savedMindmap = mindmapRepository.save(mindmap);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof Jwt)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication type");
            }
            String ownerId = ((Jwt) principal).getSubject();
            ResourceRegistrationRequest resourceRegistrationRequest = ResourceRegistrationRequest.builder()
                    .id(savedMindmap.getId())
                    .name(savedMindmap.getTitle())
                    .resourceType("mindmap")
                    .thumbnail(savedMindmap.getThumbnail())
                    .build();
            resourcePermissionApi.registerResource(resourceRegistrationRequest, ownerId);

            log.info("Successfully created mindmap with id: '{}'", savedMindmap.getId());
            MindmapCreateResponseDto response = new MindmapCreateResponseDto();
            response.setId(savedMindmap.getId());
            response.setCreatedAt(savedMindmap.getCreatedAt());
            response.setTitle(savedMindmap.getTitle());
            response.setExtraField("nodes", savedMindmap.getNodes());
            response.setExtraField("edges", savedMindmap.getEdges());
            return response;
        } catch (Exception e) {
            log.error("Failed to create mindmap with title: '{}'. Error: {}", request.getTitle(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<MindmapListResponseDto> getAllMindmaps(MindmapCollectionRequest request) {
        log.info("Retrieving mindmaps with pagination - page: {}, size: {}", request.getPage(), request.getSize());

        try {
            Pageable pageable = PageRequest
                    .of(request.getPage() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof Jwt)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication type");
            }
            String ownerId = ((Jwt) principal).getSubject();
            List<String> resourceIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "mindmap");
            log.info("Found {} mindmap resources for ownerId: '{}'", resourceIds.size(), ownerId);

            // Page<Mindmap> mindmapPage = mindmapRepository.findAll(pageable);
            Page<Mindmap> mindmapPage = mindmapRepository.findByIdIn(resourceIds, pageable);

            List<MindmapListResponseDto> content = mindmapPage.getContent()
                    .stream()
                    .map(mapper::entityToListResponse)
                    .collect(Collectors.toList());

            PaginationDto pagination = new PaginationDto(request.getPage(), mindmapPage.getSize(),
                    mindmapPage.getTotalElements(), mindmapPage.getTotalPages());

            log.info("Successfully retrieved {} mindmaps from page {} of {}",
                    content.size(),
                    pagination.getCurrentPage(),
                    pagination.getTotalPages());

            return new PaginatedResponseDto<>(content, pagination);
        } catch (Exception e) {
            log.error("Failed to retrieve paginated mindmaps. Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateMindmap(String id, MindmapUpdateRequest request, MultipartFile thumbnailFile) {
        log.info("Updating mindmap with id: '{}' (multipart)", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap existingMindmap = findMindmapById(id);

            // Process thumbnail file if provided
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                // Determine file extension and content type from uploaded file
                String contentType = thumbnailFile.getContentType();
                if (contentType == null || contentType.isEmpty()) {
                    contentType = "image/jpeg"; // Default to JPEG
                }

                String extension = contentType.equals("image/png") ? "png" : "jpg";

                // Build storage key with appropriate extension
                String storageKey = String.format("user/thumbnails/mindmap/%s.%s", id, extension);

                // Upload to R2 directly
                String uploadedKey = rustfsStorageService.uploadFile(thumbnailFile, storageKey, contentType);

                // Build CDN URL
                String cdnUrl = MediaStorageUtils.buildCdnUrl(uploadedKey, cdnDomain);

                request.setThumbnail(cdnUrl);
                // No deletion needed - putObject overwrites existing file automatically
            }

            mapper.updateEntityFromRequest(request, existingMindmap);

            mindmapRepository.save(existingMindmap);
            log.info("Successfully updated mindmap with id: '{}'", id);
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update mindmap with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateTitleAndDescriptionMindmap(String id, MindmapUpdateTitleAndDescriptionRequest request) {
        log.info("Updating mindmap title with id: '{}'", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap existingMindmap = findMindmapById(id);

            if (StringUtils.hasText(request.getTitle())) {
                existingMindmap.setTitle(request.getTitle());
            }

            if (StringUtils.hasText(request.getDescription())) {
                existingMindmap.setDescription(request.getDescription());
            }

            mindmapRepository.save(existingMindmap);
            log.info("Successfully updated mindmap title with id: '{}'", id);
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update mindmap title with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MindmapDto getMindmap(String id) {
        log.info("Retrieving mindmap with id: '{}'", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap mindmap = findMindmapById(id);
            MindmapDto response = mapper.entityToDto(mindmap);

            // Track document visit asynchronously
            String userId = getCurrentUserId();
            if (userId != null) {
                var metadata = DocumentMetadataDto.builder()
                    .userId(userId)
                    .documentId(id)
                    .type("mindmap")
                    .title(mindmap.getTitle())
                    .thumbnail(mindmap.getThumbnail())
                    .build();
                documentVisitService.trackDocumentVisit(metadata);
            }

            log.info("Successfully retrieved mindmap with id: '{}'", id);
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve mindmap with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    private Mindmap buildMindmapFromRequest(MindmapCreateRequest request) {
        Mindmap mindmap = mapper.createRequestToEntity(request);

        if (mindmap.getNodes() != null) {
            mindmap.getNodes().forEach(node -> {
                if (node.getId() == null) {
                    node.setId(UUID.randomUUID().toString());
                }
            });
        }

        if (mindmap.getEdges() != null) {
            mindmap.getEdges().forEach(edge -> {
                if (edge.getId() == null) {
                    edge.setId(UUID.randomUUID().toString());
                }
            });
        }

        return mindmap;
    }

    private Mindmap findMindmapById(String id) {
        return mindmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mindmap not found with id: " + id));
    }
}
