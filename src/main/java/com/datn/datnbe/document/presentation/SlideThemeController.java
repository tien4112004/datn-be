package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideThemeCreateRequest;
import com.datn.datnbe.document.dto.request.SlideThemeUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slide-themes")
@RequiredArgsConstructor
@Slf4j
public class SlideThemeController {

    private final SlideThemeApi slideThemeApi;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<SlideThemeResponseDto>>> getAllSlideThemes(
            @Valid @ModelAttribute SlideThemeCollectionRequest request) {

        log.info("GET /api/slide-themes - Fetching slide themes with page: {}, pageSize: {}",
                request.getPage(),
                request.getPageSize());

        var paginatedResponse = slideThemeApi.getAllSlideThemes(request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<SlideThemeResponseDto>> createSlideTheme(
            @Valid @RequestBody SlideThemeCreateRequest request) {

        log.info("POST /api/slide-themes - Creating slide theme with id: {}", request.getId());

        var response = slideThemeApi.createSlideTheme(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<SlideThemeResponseDto>> updateSlideTheme(@PathVariable String id,
            @Valid @RequestBody SlideThemeUpdateRequest request) {

        log.info("PUT /api/slide-themes/{} - Updating slide theme", id);

        var response = slideThemeApi.updateSlideTheme(id, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
