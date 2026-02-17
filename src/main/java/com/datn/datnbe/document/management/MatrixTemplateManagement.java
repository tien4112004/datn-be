package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MatrixTemplateApi;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.MatrixTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponseDto;
import com.datn.datnbe.document.entity.AssignmentMatrixEntity;
import com.datn.datnbe.document.mapper.MatrixTemplateMapper;
import com.datn.datnbe.document.repository.AssignmentMatrixTemplateRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Management service for matrix template CRUD operations.
 * Implements access control via ownerId filtering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatrixTemplateManagement implements MatrixTemplateApi {

    private final AssignmentMatrixTemplateRepository repository;
    private final MatrixTemplateMapper mapper;
    private final ObjectMapper objectMapper;
    private final SecurityContextUtils securityContextUtils;

    @Override
    public PaginatedResponseDto<MatrixTemplateResponseDto> getAllMatrixTemplates(
            MatrixTemplateCollectionRequest request) {
        // Determine ownerId filter based on bankType (following question bank pattern)
        // Default to personal if bankType is not specified
        String ownerIdFilter = null;
        String bankType = request.getBankType();

        if ("public".equalsIgnoreCase(bankType)) {
            // Public templates: ownerId IS NULL
            ownerIdFilter = null;
        } else {
            // Personal templates (default): ownerId = current user
            ownerIdFilter = securityContextUtils.getCurrentUserId();
        }

        log.info(
                "Fetching matrix templates - bankType: {} (effective: {}), ownerId: {}, page: {}, filters: subject={}, grade={}, search={}",
                request.getBankType(),
                "public".equalsIgnoreCase(bankType) ? "public" : "personal",
                ownerIdFilter,
                request.getPage(),
                request.getSubject(),
                request.getGrade(),
                request.getSearch());

        // Build query based on filters and bankType
        List<AssignmentMatrixEntity> allResults;
        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            if (ownerIdFilter != null) {
                allResults = repository.searchByOwnerIdAndName(ownerIdFilter, request.getSearch());
            } else {
                allResults = repository.searchByOwnerIdIsNullAndName(request.getSearch());
            }
        } else if (request.getSubject() != null && request.getGrade() != null) {
            if (ownerIdFilter != null) {
                allResults = repository
                        .findByOwnerIdAndSubjectAndGrade(ownerIdFilter, request.getSubject(), request.getGrade());
            } else {
                allResults = repository.findByOwnerIdIsNullAndSubjectAndGrade(request.getSubject(), request.getGrade());
            }
        } else if (request.getSubject() != null) {
            if (ownerIdFilter != null) {
                allResults = repository.findByOwnerIdAndSubject(ownerIdFilter, request.getSubject());
            } else {
                allResults = repository.findByOwnerIdIsNullAndSubject(request.getSubject());
            }
        } else if (request.getGrade() != null) {
            if (ownerIdFilter != null) {
                allResults = repository.findByOwnerIdAndGrade(ownerIdFilter, request.getGrade());
            } else {
                allResults = repository.findByOwnerIdIsNullAndGrade(request.getGrade());
            }
        } else {
            if (ownerIdFilter != null) {
                allResults = repository.findByOwnerId(ownerIdFilter);
            } else {
                allResults = repository.findByOwnerIdIsNull();
            }
        }

        // Filter only templates (name IS NOT NULL)
        List<AssignmentMatrixEntity> templates = allResults.stream()
                .filter(e -> e.getName() != null && !e.getName().isEmpty())
                .toList();

        // Manual pagination
        int page = request.getPage();
        int pageSize = request.getPageSize();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, templates.size());

        // Handle empty or out of range requests
        List<AssignmentMatrixEntity> pageContent;
        if (start >= templates.size()) {
            pageContent = List.of();
        } else {
            pageContent = templates.subList(start, end);
        }

        // Map to DTOs
        List<MatrixTemplateResponseDto> dtos = pageContent.stream().map(mapper::toResponseDto).toList();

        // Build pagination metadata
        PaginationDto pagination = PaginationDto.builder()
                .currentPage(page)
                .pageSize(pageSize)
                .totalItems((long) templates.size())
                .totalPages((int) Math.ceil((double) templates.size() / pageSize))
                .build();

        log.info("Returning {} templates - bankType: {}, ownerId: {}, page: {}/{}",
                dtos.size(),
                request.getBankType(),
                ownerIdFilter,
                page,
                pagination.getTotalPages());

        return new PaginatedResponseDto<>(dtos, pagination);
    }

    @Override
    public MatrixTemplateResponseDto getMatrixTemplateById(String id) {
        String ownerId = securityContextUtils.getCurrentUserId();

        log.info("Fetching matrix template: id={}, ownerId={}", id, ownerId);

        AssignmentMatrixEntity entity = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public MatrixTemplateResponseDto createMatrixTemplate(MatrixTemplateCreateRequest request) {
        String ownerId = securityContextUtils.getCurrentUserId();

        log.info("Creating matrix template: name={}, subject={}, grade={}, ownerId={}",
                request.getName(),
                request.getSubject(),
                request.getGrade(),
                ownerId);

        // Validate matrix JSON
        validateMatrixData(request.getMatrixData());

        // Create entity
        AssignmentMatrixEntity entity = AssignmentMatrixEntity.builder()
                .id(UUID.randomUUID().toString())
                .ownerId(ownerId)
                .name(request.getName())
                .subject(request.getSubject())
                .grade(request.getGrade())
                .matrixData(request.getMatrixData())
                .build();

        AssignmentMatrixEntity saved = repository.save(entity);

        log.info("Created matrix template: id={}, name={}, ownerId={}", saved.getId(), saved.getName(), ownerId);

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public MatrixTemplateResponseDto updateMatrixTemplate(String id, MatrixTemplateUpdateRequest request) {
        String ownerId = securityContextUtils.getCurrentUserId();

        log.info("Updating matrix template: id={}, ownerId={}", id, ownerId);

        // Load with ownership check
        AssignmentMatrixEntity entity = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        // Update fields
        if (request.getName() != null) {
            entity.setName(request.getName());
            log.debug("Updating name to: {}", request.getName());
        }
        if (request.getMatrixData() != null) {
            validateMatrixData(request.getMatrixData());
            entity.setMatrixData(request.getMatrixData());
            log.debug("Updating matrix data");
        }

        AssignmentMatrixEntity updated = repository.save(entity);

        log.info("Updated matrix template: id={}, ownerId={}", id, ownerId);

        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteMatrixTemplate(String id) {
        String ownerId = securityContextUtils.getCurrentUserId();

        log.info("Deleting matrix template: id={}, ownerId={}", id, ownerId);

        AssignmentMatrixEntity entity = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        repository.delete(entity);

        log.info("Deleted matrix template: id={}, ownerId={}", id, ownerId);
    }

    /**
     * Validate that matrixData is valid JSON and can be parsed to AssignmentMatrixDto.
     *
     * @param matrixData JSON string to validate
     * @throws IllegalArgumentException if JSON is invalid
     */
    private void validateMatrixData(String matrixData) {
        try {
            objectMapper.readValue(matrixData, AssignmentMatrixDto.class);
        } catch (Exception e) {
            log.error("Invalid matrix data JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid matrix data JSON: " + e.getMessage(), e);
        }
    }
}
