package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.PexelsImageSearchRequest;
import com.datn.datnbe.document.management.ImageManagement;
import com.datn.datnbe.document.management.ImageSearchManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageController {
    ImageManagement imageManagement;
    ImageSearchManagement imageSearchManagement;

    @GetMapping
    public ResponseEntity<AppResponseDto> getImages(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var response = imageManagement.getImages(pageable);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto> getImageById(@PathVariable Long id) {
        var response = imageManagement.getImageById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Search images from Pexels
     * Handles Pexels API rate limits gracefully
     */
    @PostMapping("/search-pexels")
    public ResponseEntity<AppResponseDto> searchPexelsImages(@Valid @RequestBody PexelsImageSearchRequest request) {

        log.info("POST /api/images/search-pexels - Searching Pexels with query: {}, orientation: {}, page: {}",
                request.getQuery(),
                request.getOrientation(),
                request.getPage());

        var response = imageSearchManagement.searchImages(request);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }
}
