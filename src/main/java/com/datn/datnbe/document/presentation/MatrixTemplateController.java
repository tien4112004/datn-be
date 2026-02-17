package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MatrixTemplateApi;
import com.datn.datnbe.document.dto.request.MatrixTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for matrix template operations.
 * Provides endpoints for creating, reading, updating, and deleting matrix templates.
 */
@RestController
@RequestMapping("/api/matrix-templates")
@RequiredArgsConstructor
@Slf4j
public class MatrixTemplateController {

    private final MatrixTemplateApi matrixTemplateApi;

    /**
     * Get all matrix templates with pagination and filtering.
     *
     * @param request Collection request with pagination and filters
     * @return Paginated list of templates
     */
    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<MatrixTemplateResponseDto>>> getAllMatrixTemplates(
            @Valid @ModelAttribute MatrixTemplateCollectionRequest request) {
        log.info("GET /api/matrix-templates - page: {}, size: {}, subject: {}, grade: {}, search: {}, bankType: {}",
                request.getPage(),
                request.getPageSize(),
                request.getSubject(),
                request.getGrade(),
                request.getSearch(),
                request.getBankType());

        var response = matrixTemplateApi.getAllMatrixTemplates(request);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    /**
     * Get single matrix template by ID.
     *
     * @param id Template ID
     * @return Template details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<MatrixTemplateResponseDto>> getMatrixTemplateById(@PathVariable String id) {
        log.info("GET /api/matrix-templates/{}", id);

        var response = matrixTemplateApi.getMatrixTemplateById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Create new matrix template.
     *
     * @param request Create request
     * @return Created template
     */
    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<MatrixTemplateResponseDto>> createMatrixTemplate(
            @Valid @RequestBody MatrixTemplateCreateRequest request) {
        log.info("POST /api/matrix-templates - name: {}, subject: {}, grade: {}",
                request.getName(),
                request.getSubject(),
                request.getGrade());

        var response = matrixTemplateApi.createMatrixTemplate(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    /**
     * Update existing matrix template.
     *
     * @param id Template ID
     * @param request Update request
     * @return Updated template
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AppResponseDto<MatrixTemplateResponseDto>> updateMatrixTemplate(@PathVariable String id,
            @Valid @RequestBody MatrixTemplateUpdateRequest request) {
        log.info("PATCH /api/matrix-templates/{} - name: {}", id, request.getName());

        var response = matrixTemplateApi.updateMatrixTemplate(id, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Delete matrix template.
     *
     * @param id Template ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> deleteMatrixTemplate(@PathVariable String id) {
        log.info("DELETE /api/matrix-templates/{}", id);

        matrixTemplateApi.deleteMatrixTemplate(id);

        return ResponseEntity.ok(AppResponseDto.success(null));
    }
}
