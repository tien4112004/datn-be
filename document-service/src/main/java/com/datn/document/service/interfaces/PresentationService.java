package com.datn.document.service.interfaces;

import com.datn.document.dto.common.PaginatedResponseDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.request.PresentationUpdateRequest;
import com.datn.document.dto.request.PresentationUpdateTitleRequest;
import com.datn.document.dto.request.PresentationCollectionRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.dto.response.PresentationListResponseDto;
import com.datn.document.dto.response.PresentationUpdateResponseDto;

import java.util.List;

public interface PresentationService {
    PresentationCreateResponseDto createPresentation(PresentationCreateRequest request);
    PresentationUpdateResponseDto updatePresentation(String id, PresentationUpdateRequest request);
    PresentationUpdateResponseDto updateTitlePresentation(String id, PresentationUpdateTitleRequest request);
    List<PresentationListResponseDto> getAllPresentations();
    PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(PresentationCollectionRequest request);
}
