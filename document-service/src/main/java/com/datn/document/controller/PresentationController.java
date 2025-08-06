package com.datn.document.controller;

import com.datn.document.dto.common.AppResponseDto;
import com.datn.document.dto.common.PaginatedResponseDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.request.PresentationCollectionRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.dto.response.PresentationListResponseDto;
import com.datn.document.service.interfaces.PresentationService;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
public class PresentationController {

    private final PresentationService presentationService;

    @PostMapping({ "", "/" })
    public ResponseEntity<AppResponseDto<PresentationCreateResponseDto>> createPresentation(
            @Valid @RequestBody PresentationCreateRequest request) {
        PresentationCreateResponseDto response = presentationService.createPresentation(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping
    public ResponseEntity<AppResponseDto<List<PresentationListResponseDto>>> getAllPresentations() {
        List<PresentationListResponseDto> presentations = presentationService.getAllPresentations();
        return ResponseEntity.ok(AppResponseDto.success(presentations));
    }

    @GetMapping("/collection")
    public ResponseEntity<AppResponseDto<PaginatedResponseDto<PresentationListResponseDto>>> getAllPresentationsCollection(
            @ModelAttribute PresentationCollectionRequest request) {
        
        PaginatedResponseDto<PresentationListResponseDto> response =
            presentationService.getAllPresentations(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
