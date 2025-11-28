package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface SlideTemplateApi {

    PaginatedResponseDto<SlideTemplateResponseDto> getAllSlideTemplates(SlideTemplateCollectionRequest request);

    SlideTemplateResponseDto createSlideTemplate(SlideTemplateCreateRequest request);

    SlideTemplateResponseDto updateSlideTemplate(String id, SlideTemplateUpdateRequest request);
}
