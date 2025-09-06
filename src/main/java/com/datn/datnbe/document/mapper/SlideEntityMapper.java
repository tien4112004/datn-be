package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.request.SlideCreateRequest;
import com.datn.datnbe.document.dto.request.SlideUpdateRequest;
import com.datn.datnbe.document.entity.valueobject.Slide;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, uses = {
        SlideElementMapper.class
        // , SlideBackgroundMapper.class
})
@Named("SlideEntityMapper")
public interface SlideEntityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "background", source = "background")
    @Mapping(target = "elements", source = "elements", qualifiedByName = "toEntityList")
    @Named("toEntity")
    Slide toEntity(SlideDto slideDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "elements", source = "elements", qualifiedByName = "toDto")
    @Mapping(target = "background", source = "background")
    @Named("toDto")
    SlideDto toDto(Slide slide);

    @IterableMapping(qualifiedByName = "toDto")
    @Named("toDtoList")
    List<SlideDto> toDtoList(List<Slide> slides);

    @IterableMapping(qualifiedByName = "toEntity")
    @Named("toEntityList")
    List<Slide> toEntityList(List<SlideDto> slideDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "elements", source = "elements", qualifiedByName = "createRequestToEntityList")
    @Mapping(target = "background", source = "background")
    @Named("createRequestToEntityList")
    Slide createRequestToEntity(SlideCreateRequest request);

    @IterableMapping(qualifiedByName = "createRequestToEntityList")
    @Named("createRequestToEntityList")
    List<Slide> createRequestToEntityList(List<SlideCreateRequest> slideCreateRequests);

    @Mapping(target = "id", source = "slideId")
    @Mapping(target = "elements", source = "elements", qualifiedByName = "updateRequestToEntityList")
    @Mapping(target = "background", source = "background")
    @Named("updateRequestToEntity")
    Slide updateRequestToEntity(SlideUpdateRequest request);

    @IterableMapping(qualifiedByName = "updateRequestToEntity")
    @Named("updateRequestToEntityList")
    List<Slide> updateRequestToEntityList(List<SlideUpdateRequest> slideUpdateRequests);

}
