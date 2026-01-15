package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.ImagesApi;
import com.datn.datnbe.document.dto.request.ImageCollectionRequest;
import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.document.mapper.MediaEntityMapper;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageManagement implements ImagesApi {

    MediaRepository mediaRepository;
    MediaEntityMapper mediaMapper;
    SecurityContextUtils securityContextUtils;

    @Override
    public PaginatedResponseDto<MediaResponseDto> getImages(ImageCollectionRequest request) {
        String ownerId = securityContextUtils.getCurrentUserId();

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(request.getValidatedSort())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest
                .of(request.getPage() - 1, request.getPageSize(), Sort.by(sortDirection, "createdAt"));

        var medias = mediaRepository.findByOwnerIdAndMediaType(ownerId, MediaType.IMAGE, pageable);

        var paginationDto = new PaginationDto(request.getPage(), medias.getSize(), medias.getTotalElements(),
                medias.getTotalPages());

        return PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(medias.stream().map(mediaMapper::toResponseDto).toList())
                .build();
    }

    @Override
    public MediaResponseDto getImageById(Long imageId) {
        return mediaMapper.toResponseDto(mediaRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.MEDIA_NOT_FOUND, "Image not found")));
    }
}
