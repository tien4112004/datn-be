package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
