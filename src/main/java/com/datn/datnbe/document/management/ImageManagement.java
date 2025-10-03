package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.ImagesApi;
import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.document.mapper.MediaEntityMapper;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageManagement implements ImagesApi {

    MediaRepository mediaRepository;
    MediaEntityMapper mediaMapper;

    @Override
    public AppResponseDto<PaginatedResponseDto<MediaResponseDto>> getImages(Pageable pageable) {
        var medias = mediaRepository.findByMediaType(MediaType.IMAGE, pageable);

        var paginationDto = PaginationDto.getFromPageable(pageable);
        paginationDto.setTotalItems(medias.getTotalElements());
        paginationDto.setTotalPages(medias.getTotalPages());

        var paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(medias.stream().map(mediaMapper::toResponseDto).toList())
                .build();

        return AppResponseDto.success(paginatedResponse);
    }

    @Override
    public AppResponseDto<MediaResponseDto> getImageById(Long imageId) {
        return AppResponseDto.success(mediaMapper.toResponseDto(mediaRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND, "Image not found"))));
    }
}
