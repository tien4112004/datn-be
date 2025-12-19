package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.ArtStyleApi;
import com.datn.datnbe.document.dto.request.ArtStyleCollectionRequest;
import com.datn.datnbe.document.dto.request.ArtStyleCreateRequest;
import com.datn.datnbe.document.dto.request.ArtStyleUpdateRequest;
import com.datn.datnbe.document.dto.response.ArtStyleResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/art-styles")
@RequiredArgsConstructor
@Slf4j
public class ArtStyleController {

    private final ArtStyleApi artStyleApi;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<ArtStyleResponseDto>>> getAllArtStyles(
            @Valid @ModelAttribute ArtStyleCollectionRequest request) {

        log.info("GET /api/art-styles - Fetching art styles with page: {}, pageSize: {}",
                request.getPage(),
                request.getPageSize());

        var paginatedResponse = artStyleApi.getAllArtStyles(request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<ArtStyleResponseDto>> createArtStyle(
            @Valid @RequestBody ArtStyleCreateRequest request) {

        log.info("POST /api/art-styles - Creating art style with id: {}", request.getId());

        var response = artStyleApi.createArtStyle(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<ArtStyleResponseDto>> updateArtStyle(@PathVariable String id,
            @Valid @RequestBody ArtStyleUpdateRequest request) {

        log.info("PUT /api/art-styles/{} - Updating art style", id);

        var response = artStyleApi.updateArtStyle(id, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
