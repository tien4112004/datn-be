package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.SlideTemplateApi;
import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slide-templates")
@RequiredArgsConstructor
@Slf4j
public class SlideTemplateController {

    private final SlideTemplateApi slideTemplateApi;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<SlideTemplateResponseDto>>> getAllSlideTemplates(
            @Valid @ModelAttribute SlideTemplateCollectionRequest request) {

        log.info("GET /api/slide-templates - Fetching slide templates with page: {}, pageSize: {}",
                request.getPage(),
                request.getPageSize());

        var paginatedResponse = slideTemplateApi.getAllSlideTemplates(request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<SlideTemplateResponseDto>> createSlideTemplate(
            @Valid @RequestBody SlideTemplateCreateRequest request) {

        log.info("POST /api/slide-templates - Creating slide template with id: {}", request.getId());

        var response = slideTemplateApi.createSlideTemplate(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<SlideTemplateResponseDto>> updateSlideTemplate(@PathVariable String id,
            @Valid @RequestBody SlideTemplateUpdateRequest request) {

        log.info("PUT /api/slide-templates/{} - Updating slide template", id);

        var response = slideTemplateApi.updateSlideTemplate(id, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlideTemplate(@PathVariable String id) {

        log.info("DELETE /api/slide-templates/{} - Deleting slide template", id);

        slideTemplateApi.deleteSlideTemplate(id);

        return ResponseEntity.noContent().build();
    }
}
