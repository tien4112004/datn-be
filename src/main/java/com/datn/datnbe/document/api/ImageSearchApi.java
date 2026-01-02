package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.PexelsImageSearchRequest;
import com.datn.datnbe.document.dto.response.ImageSearchResultDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface ImageSearchApi {
    PaginatedResponseDto<ImageSearchResultDto> searchImages(PexelsImageSearchRequest request);
}
