package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface ImagesApi {
    PaginatedResponseDto<MediaResponseDto> getImages(Pageable pageable);

    MediaResponseDto getImageById(Long imageId);
}
