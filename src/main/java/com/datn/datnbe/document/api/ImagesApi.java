package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

public interface ImagesApi {
    AppResponseDto<PaginatedResponseDto<MediaResponseDto>> getImages(Pageable pageable);

    AppResponseDto<MediaResponseDto> getImageById(Long imageId);
}
