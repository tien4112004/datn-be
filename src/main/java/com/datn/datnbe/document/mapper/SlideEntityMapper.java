package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.request.SlideCreateRequest;
import com.datn.datnbe.document.dto.request.SlideUpdateRequest;
import com.datn.datnbe.document.entity.valueobject.Slide;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
@Named("SlideEntityMapper")
public interface SlideEntityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "extraFields", source = "extraFields")
    @Named("toEntity")
    Slide toEntity(SlideDto slideDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "extraFields", source = "extraFields")
    @Named("toDto")
    SlideDto toDto(Slide slide);

    @IterableMapping(qualifiedByName = "toDto")
    @Named("toDtoList")
    List<SlideDto> toDtoList(List<Slide> slides);

    @IterableMapping(qualifiedByName = "toEntity")
    @Named("toEntityList")
    List<Slide> toEntityList(List<SlideDto> slideDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "extraFields", source = "extraFields")
    @Named("createRequestToEntity")
    Slide createRequestToEntity(SlideCreateRequest request);

    @IterableMapping(qualifiedByName = "createRequestToEntity")
    @Named("createRequestToEntityList")
    List<Slide> createRequestToEntityList(List<SlideCreateRequest> slideCreateRequests);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "extraFields", source = "extraFields")
    @Named("updateRequestToEntity")
    Slide updateRequestToEntity(SlideUpdateRequest request);

    @IterableMapping(qualifiedByName = "updateRequestToEntity")
    @Named("updateRequestToEntityList")
    List<Slide> updateRequestToEntityList(List<SlideUpdateRequest> slideUpdateRequests);

}
