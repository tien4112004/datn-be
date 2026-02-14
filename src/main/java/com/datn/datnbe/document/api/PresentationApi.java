package com.datn.datnbe.document.api;

import org.springframework.web.multipart.MultipartFile;

import com.datn.datnbe.document.dto.request.*;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface PresentationApi {
    PresentationCreateResponseDto createPresentation(PresentationCreateRequest request);

    PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(PresentationCollectionRequest request);

    void updatePresentation(String id, PresentationUpdateRequest request, MultipartFile thumbnailFile);

    void updateTitlePresentation(String id, PresentationUpdateTitleRequest request);

    PresentationDto getPresentation(String id);

    void deletePresentation(String id);
}
