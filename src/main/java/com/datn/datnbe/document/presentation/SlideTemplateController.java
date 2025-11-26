package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.SlideTemplateApi;
import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
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
}
