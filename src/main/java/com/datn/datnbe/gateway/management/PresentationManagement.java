package com.datn.datnbe.gateway.management;

import java.util.List;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.PresentationCollectionRequest;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateTitleRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.gateway.dto.AppResponseDto;
import com.datn.datnbe.gateway.dto.PaginatedResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
public class PresentationManagement {

    private final PresentationApi presentationApi;

    @PostMapping({ "", "/" })
    public ResponseEntity<AppResponseDto<PresentationCreateResponseDto>> createPresentation(
            @Valid @RequestBody PresentationCreateRequest request) {
        PresentationCreateResponseDto response = presentationApi.createPresentation(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<AppResponseDto<List<PresentationListResponseDto>>> getAllPresentations() {
        List<PresentationListResponseDto> presentations = presentationApi.getAllPresentations();
        return ResponseEntity.ok(AppResponseDto.success(presentations));
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<AppResponseDto<List<PresentationListResponseDto>>> getAllPresentationsCollection(
            @Valid @ModelAttribute PresentationCollectionRequest request) {

        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = presentationApi
                .getAllPresentations(request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> updatePresentation(
            @PathVariable String id,
            @Valid @RequestBody PresentationUpdateRequest request) {
        presentationApi.updatePresentation(id, request);
        return ResponseEntity.ok(AppResponseDto.success(null));
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<AppResponseDto<Void>> updateTitlePresentation(
            @PathVariable String id,
            @Valid @RequestBody PresentationUpdateTitleRequest request) {
        presentationApi.updateTitlePresentation(id, request);
        return ResponseEntity.ok(AppResponseDto.success(null));
    }
}
