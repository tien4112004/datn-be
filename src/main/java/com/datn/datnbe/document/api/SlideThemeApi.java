package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface SlideThemeApi {

    PaginatedResponseDto<SlideThemeResponseDto> getAllSlideThemes(SlideThemeCollectionRequest request);
}
