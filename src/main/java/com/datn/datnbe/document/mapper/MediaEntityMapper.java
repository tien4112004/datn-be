package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaEntityMapper {

    MediaResponseDto toResponseDto(Media media);
}
