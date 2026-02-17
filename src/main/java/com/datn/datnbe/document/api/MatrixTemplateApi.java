package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.MatrixTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

/**
 * API interface for matrix template operations.
 * Provides CRUD operations for managing matrix templates.
 */
public interface MatrixTemplateApi {

    /**
     * Get all matrix templates with pagination and filtering.
     * Filters templates by ownerId (current user) and optionally by subject, grade, search term.
     *
     * @param request Collection request with pagination and filter parameters
     * @return Paginated list of matrix templates
     */
    PaginatedResponseDto<MatrixTemplateResponseDto> getAllMatrixTemplates(MatrixTemplateCollectionRequest request);

    /**
     * Get single matrix template by ID.
     * Verifies ownership before returning.
     *
     * @param id Template ID
     * @return Matrix template details
     */
    MatrixTemplateResponseDto getMatrixTemplateById(String id);

    /**
     * Create new matrix template.
     * Sets ownerId to current user.
     *
     * @param request Create request with template data
     * @return Created matrix template
     */
    MatrixTemplateResponseDto createMatrixTemplate(MatrixTemplateCreateRequest request);

    /**
     * Update existing matrix template.
     * Verifies ownership before updating.
     *
     * @param id Template ID
     * @param request Update request with new values
     * @return Updated matrix template
     */
    MatrixTemplateResponseDto updateMatrixTemplate(String id, MatrixTemplateUpdateRequest request);

    /**
     * Delete matrix template.
     * Verifies ownership before deleting.
     *
     * @param id Template ID
     */
    void deleteMatrixTemplate(String id);
}
