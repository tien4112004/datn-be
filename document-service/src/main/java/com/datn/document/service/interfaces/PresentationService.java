package com.datn.document.service.interfaces;

import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;

public interface PresentationService {
    PresentationCreateResponseDto createPresentation(PresentationCreateRequest request);
}
