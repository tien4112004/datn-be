package com.datn.datnbe.ai.mapper;

import com.datn.datnbe.ai.dto.response.ImageResponseDto;
import com.datn.datnbe.document.api.MediaStorageApi;
import org.mapstruct.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ImageGenerateMapper {

    @Named("toCdnUrl")
    default String toCdnUrl(MultipartFile image, @Context MediaStorageApi mediaStorageApi) {
        return mediaStorageApi.upload(image).getCdnUrl();
    }

    @IterableMapping(qualifiedByName = "toCdnUrl")
    List<String> toCdnUrls(List<MultipartFile> images, @Context MediaStorageApi mediaStorageApi);

    default ImageResponseDto toImageResponseDto(List<MultipartFile> images, @Context MediaStorageApi mediaStorageApi) {
        return ImageResponseDto.builder().cdnUrls(toCdnUrls(images, mediaStorageApi)).build();
    }
}
