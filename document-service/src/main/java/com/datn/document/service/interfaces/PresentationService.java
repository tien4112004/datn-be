package com.datn.document.service.interfaces;

import com.datn.document.dto.common.PaginatedResponseDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.request.PresentationUpdateRequest;
import com.datn.document.dto.request.PresentationCollectionRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.dto.response.PresentationListResponseDto;

import java.util.List;

public interface PresentationService {
    PresentationCreateResponseDto createPresentation(PresentationCreateRequest request);
    PresentationCreateResponseDto updatePresentation(String id, PresentationUpdateRequest request);
    List<PresentationListResponseDto> getAllPresentations();
    PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(PresentationCollectionRequest request);
}
