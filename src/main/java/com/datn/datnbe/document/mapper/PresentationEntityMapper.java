package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.SlideDto.SlideBackgroundDto;
import com.datn.datnbe.document.dto.SlideDto.SlideElementDto;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.dto.response.PresentationUpdateResponseDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.entity.valueobject.Slide;
import com.datn.datnbe.document.entity.valueobject.SlideBackground;
import com.datn.datnbe.document.entity.valueobject.SlideElement;
import com.datn.datnbe.document.enums.SlideElementType;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, uses = {
        SlideEntityMapper.class})
@Named("PresentationEntityMapper")
public interface PresentationEntityMapper {

    @Mapping(target = "title", expression = "java((request.getTitle() == null || request.getTitle().isEmpty()) ? \"Untitled Presentation\" : request.getTitle())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "slides", source = "slides", qualifiedByName = "toEntityList")
    Presentation createRequestToEntity(PresentationCreateRequest request);

    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "slides", source = "slides", qualifiedByName = "toEntityList")
    void updateEntity(PresentationUpdateRequest request, @MappingTarget Presentation presentation);

    @Mapping(target = "presentation", source = "slides")
    PresentationCreateResponseDto toResponseDto(Presentation entity);

    @Mapping(target = "presentation", source = "slides")
    PresentationUpdateResponseDto toUpdateResponseDto(Presentation entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "thumbnail", expression = "java(entity.getSlides() != null && !entity.getSlides().isEmpty() ? slideToSlideDto(entity.getSlides().get(0)) : null)")
    PresentationListResponseDto toListResponseDto(Presentation entity);

    @Mapping(target = "slides", source = "slides", qualifiedByName = "toDtoList")
    PresentationDto toDetailedDto(Presentation entity);

    // Helper methods for null safety
    default <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

}