package com.datn.datnbe.document.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.datnbe.document.dto.request.PexelsImageSearchRequest;
import com.datn.datnbe.document.management.ImageSearchManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ToolsController {

    ImageSearchManagement imageSearchManagement;

    /**
     * Search images from Pexels
     * Handles Pexels API rate limits gracefully
     *
     * @deprecated Use /api/images/search-pexels instead
     * This endpoint is maintained for backward compatibility only
     */
    @Deprecated
    @PostMapping("/img_search")
    public ResponseEntity<AppResponseDto> searchImages(@Valid @RequestBody PexelsImageSearchRequest request) {

        log.warn("DEPRECATED: /api/tools/img_search called. Please use /api/images/search-pexels");

        var response = imageSearchManagement.searchImages(request);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }
}
