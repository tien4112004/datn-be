package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.ImageCollectionRequest;
import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface ImagesApi {
    PaginatedResponseDto<MediaResponseDto> getImages(ImageCollectionRequest request);

    MediaResponseDto getImageById(Long imageId);
}
