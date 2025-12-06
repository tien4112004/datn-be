package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationUpdateRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.dto.response.PresentationUpdateResponseDto;
import com.datn.datnbe.document.entity.Presentation;
import java.util.ArrayList;
import java.util.List;

import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, uses = {
        SlideEntityMapper.class})
@Named("PresentationEntityMapper")
public interface PresentationEntityMapper {

    @Mapping(target = "id", expression = "java(request.getId() != null && !request.getId().isEmpty() ? java.util.UUID.fromString(request.getId()) : null)")
    @Mapping(target = "title", expression = "java((request.getTitle() == null || request.getTitle().isEmpty()) ? \"Untitled Presentation\" : request.getTitle())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "slides", source = "slides", qualifiedByName = "toEntityList")
    @Mapping(target = "isParsed", source = "isParsed", defaultValue = "true")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "deletedAt", ignore = true)
    Presentation createRequestToEntity(PresentationCreateRequest request);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "slides", source = "slides", qualifiedByName = "toEntityList")
    @Mapping(target = "isParsed", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(PresentationUpdateRequest request, @MappingTarget Presentation presentation);

    @Mapping(target = "id", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "slides", source = "slides")
    PresentationCreateResponseDto toResponseDto(Presentation entity);

    @Mapping(target = "id", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "slides", source = "slides")
    PresentationUpdateResponseDto toUpdateResponseDto(Presentation entity);

    @Mapping(target = "id", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "thumbnail", expression = "java(entity.getSlides() != null && !entity.getSlides().isEmpty() ? slideToSlideDto(entity.getSlides().get(0)) : null)")
    PresentationListResponseDto toListResponseDto(Presentation entity);

    @Mapping(target = "id", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "slides", source = "slides", qualifiedByName = "toDtoList")
    PresentationDto toDetailedDto(Presentation entity);

    // Helper methods for null safety
    default <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
