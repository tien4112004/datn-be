package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.ArtStyleCollectionRequest;
import com.datn.datnbe.document.dto.request.ArtStyleCreateRequest;
import com.datn.datnbe.document.dto.request.ArtStyleUpdateRequest;
import com.datn.datnbe.document.dto.response.ArtStyleResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface ArtStyleApi {

    PaginatedResponseDto<ArtStyleResponseDto> getAllArtStyles(ArtStyleCollectionRequest request);

    ArtStyleResponseDto createArtStyle(ArtStyleCreateRequest request);

    ArtStyleResponseDto updateArtStyle(String id, ArtStyleUpdateRequest request);
}
