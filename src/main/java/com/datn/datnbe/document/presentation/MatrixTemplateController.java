package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MatrixTemplateApi;
import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matrix-templates")
@RequiredArgsConstructor
public class MatrixTemplateController {

    private final MatrixTemplateApi matrixTemplateApi;

    @PostMapping
    public ResponseEntity<AppResponseDto<MatrixTemplateResponse>> createMatrixTemplate(
            @RequestBody MatrixTemplateCreateRequest request) {
        MatrixTemplateResponse response = matrixTemplateApi.createMatrixTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping
    public ResponseEntity<AppResponseDto<List<MatrixTemplateResponse>>> getMatrixTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponseDto<MatrixTemplateResponse> paginated = matrixTemplateApi.getMatrixTemplates(page, size);
        return ResponseEntity.ok(AppResponseDto.successWithPagination(paginated.getData(), paginated.getPagination()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<MatrixTemplateResponse>> getMatrixTemplateById(@PathVariable String id) {
        MatrixTemplateResponse response = matrixTemplateApi.getMatrixTemplateById(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<MatrixTemplateResponse>> updateMatrixTemplate(@PathVariable String id,
            @RequestBody MatrixTemplateUpdateRequest request) {
        MatrixTemplateResponse response = matrixTemplateApi.updateMatrixTemplate(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatrixTemplate(@PathVariable String id) {
        matrixTemplateApi.deleteMatrixTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
