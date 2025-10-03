package com.datn.datnbe.ai.mapper;

import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.document.api.MediaStorageApi;
import org.mapstruct.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ImageGenerateMapper {

    @Named("toElement")
    default Map<String, Object> toElement(MultipartFile image, @Context MediaStorageApi mediaStorageApi) {
        var uploaded = mediaStorageApi.upload(image);
        return Map.of("id", uploaded.getId(), "cdnUrl", uploaded.getCdnUrl());
    }

    @IterableMapping(qualifiedByName = "toElement")
    List<Map<String, Object>> toElements(List<MultipartFile> images, @Context MediaStorageApi mediaStorageApi);

    default ImageResponseDto toImageResponseDto(List<MultipartFile> images, @Context MediaStorageApi mediaStorageApi) {
        return ImageResponseDto.builder().images(toElements(images, mediaStorageApi)).build();
    }
}
