package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.SlideDto.SlideElementDto;
import com.datn.datnbe.document.dto.request.SlideCreateRequest.SlideElementCreateRequest;
import com.datn.datnbe.document.dto.request.SlideUpdateRequest.SlideElementUpdateRequest;
import com.datn.datnbe.document.entity.valueobject.SlideElement;
import com.datn.datnbe.document.enums.SlideElementType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL, imports = {
        SlideElementType.class})
@Named("SlideElementMapper")
public interface SlideElementMapper {

    @Named("toEntity")
    @Mapping(target = "type", expression = "java(SlideElementType.fromValue(elementDto.getType()))")
    SlideElement toEntity(SlideElementDto elementDto);

    @Named("toDto")
    @Mapping(target = "type", expression = "java(element.getType() != null ? element.getType().getValue() : null)")
    SlideElementDto toDto(SlideElement element);

    @IterableMapping(qualifiedByName = "toEntity")
    @Named("toEntityList")
    List<SlideElement> toEntityList(List<SlideElementDto> elementDtos);

    @IterableMapping(qualifiedByName = "toDto")
    @Named("toDtoList")
    List<SlideElementDto> toDtoList(List<SlideElement> elements);

    // Create request
    @Named("createRequestToEntity")
    @Mapping(target = "type", expression = "java(SlideElementType.fromValue(createRequest.getType()))")
    SlideElement createRequestToEntity(SlideElementCreateRequest createRequest);

    @IterableMapping(qualifiedByName = "createRequestToEntity")
    @Named("createRequestToEntityList")
    List<SlideElement> createRequestToEntityList(List<SlideElementCreateRequest> elementDtos);

    // Update request
    @Named("updateRequestToEntity")
    @Mapping(target = "type", expression = "java(SlideElementType.fromValue(updateRequest.getType()))")
    SlideElement updateRequestToEntity(SlideElementUpdateRequest updateRequest);

    @IterableMapping(qualifiedByName = "updateRequestToEntity")
    @Named("updateRequestToEntityList")
    List<SlideElement> updateRequestToEntityList(List<SlideElementUpdateRequest> elementDtos);

//    default SlideElementType convertToSlideElementType(String type) {
//        if (type == null) {
//            return null;
//        }
//
//        try {
//            return SlideElementType.valueOf(type.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            return null;
//        }
//    }

}