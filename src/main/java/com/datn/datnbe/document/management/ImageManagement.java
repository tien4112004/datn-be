package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.api.ImagesApi;
import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.document.mapper.MediaEntityMapper;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageManagement implements ImagesApi {

    MediaRepository mediaRepository;
    MediaEntityMapper mediaMapper;
    ResourcePermissionApi resourcePermissionApi;

    @Override
    public PaginatedResponseDto<MediaResponseDto> getImages(Pageable pageable) {
        //        var medias = mediaRepository.findByMediaType(MediaType.IMAGE, pageable);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerId = ((Jwt) authentication.getPrincipal()).getSubject();
        List<String> resourceIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "image");

        var medias = mediaRepository.findByMediaTypeWhereIn(MediaType.IMAGE.getExtensions(), resourceIds, pageable);

        var paginationDto = PaginationDto.getFromPageable(pageable);
        paginationDto.setTotalItems(medias.getTotalElements());
        paginationDto.setTotalPages(medias.getTotalPages());

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
