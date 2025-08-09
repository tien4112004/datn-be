package com.datn.document.mapper;

import com.datn.document.dto.SlideDto;
import com.datn.document.dto.SlideDto.SlideElementDto;
import com.datn.document.dto.SlideDto.SlideBackgroundDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.dto.response.PresentationListResponseDto;
import com.datn.document.entity.Presentation;
import com.datn.document.entity.valueobject.Slide;
import com.datn.document.entity.valueobject.SlideBackground;
import com.datn.document.entity.valueobject.SlideElement;
import com.datn.document.enums.SlideElementType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PresentationEntityMapper {

    @Mapping(target = "title", expression = "java(request.getTitle() != null ? request.getTitle() : \"Untitled Presentation\")")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    Presentation toEntity(PresentationCreateRequest request);

    @Mapping(target = "presentation", source = "slides")
    PresentationCreateResponseDto toResponseDto(Presentation entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "thumbnail", expression = "java(entity.getSlides() != null && !entity.getSlides().isEmpty() ? mapSlideToDto(entity.getSlides().get(0)) : null)")
    PresentationListResponseDto toListResponseDto(Presentation entity);

    Slide mapSlideToEntity(SlideDto slideDto);

    SlideDto mapSlideToDto(Slide slide);

    @Mapping(target = "type", source = "type", qualifiedByName = "slideElementTypeToString")
    SlideElement mapElementToEntity(SlideElementDto elementDto);

    @Mapping(target = "type", source = "type", qualifiedByName = "stringToSlideElementType")
    SlideElementDto mapElementToDto(SlideElement element);

    SlideBackground mapBackgroundToEntity(SlideBackgroundDto backgroundDto);

    SlideBackgroundDto mapBackgroundToDto(SlideBackground background);

    @Named("slideElementTypeToString")
    default String slideElementTypeToString(SlideElementType type) {
        return type != null ? type.getValue() : null;
    }

    @Named("stringToSlideElementType")
    default SlideElementType stringToSlideElementType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return SlideElementType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}