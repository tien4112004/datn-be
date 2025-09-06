package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.*;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import java.util.List;

public interface PresentationApi {
    PresentationCreateResponseDto createPresentation(PresentationCreateRequest request);

    List<PresentationListResponseDto> getAllPresentations();

    PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(PresentationCollectionRequest request);

    void updatePresentation(String id, PresentationUpdateRequest request);

    void updateTitlePresentation(String id, PresentationUpdateTitleRequest request);

    PresentationDto getPresentation(String id);

    void updatePresentationParsingStatus(String id);
}
