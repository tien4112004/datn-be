package com.datn.datnbe.document.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.api.SlidesApi;
import com.datn.datnbe.document.dto.request.PresentationCollectionRequest;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateTitleRequest;
import com.datn.datnbe.document.dto.request.SlidesUpsertRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import com.datn.datnbe.sharedkernel.security.annotation.RequireDocumentPermission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
@Slf4j
public class PresentationController {

    private final PresentationApi presentationApi;
    private final SlidesApi slidesApi;

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<PresentationCreateResponseDto>> createPresentation(
            @Valid @RequestBody PresentationCreateRequest request) {
        PresentationCreateResponseDto response = presentationApi.createPresentation(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<PresentationListResponseDto>>> getAllPresentationsCollection(
            @Valid @ModelAttribute PresentationCollectionRequest request) {

        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = presentationApi
                .getAllPresentations(request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PutMapping("/{id}")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<Void>> updatePresentation(@PathVariable String id,
            @Valid @RequestBody PresentationUpdateRequest request) {
        presentationApi.updatePresentation(id, request);
        return ResponseEntity.noContent().build();
    }

    @Idempotent
    @PutMapping("/{id}/slides")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<Void>> upsertSlides(@PathVariable String id,
            @Valid @RequestBody SlidesUpsertRequest request) {
        slidesApi.upsertSlides(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/title")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<Void>> updateTitlePresentation(@PathVariable String id,
            @Valid @RequestBody PresentationUpdateTitleRequest request) {
        presentationApi.updateTitlePresentation(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @RequireDocumentPermission
    public ResponseEntity<AppResponseDto<PresentationDto>> getPresentation(@PathVariable String id) {
        PresentationDto response = presentationApi.getPresentation(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PatchMapping("/{id}/parse")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<Void> updatePresentationParsingStatus(@PathVariable String id) {
        presentationApi.updatePresentationParsingStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<Void> deletePresentation(@PathVariable String id) {
        presentationApi.deletePresentation(id);
        return ResponseEntity.noContent().build();
    }

}
