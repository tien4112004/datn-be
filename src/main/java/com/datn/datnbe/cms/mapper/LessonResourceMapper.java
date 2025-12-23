package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.LessonResourceCreateRequest;
import com.datn.datnbe.cms.dto.response.LessonResourceResponseDto;
import com.datn.datnbe.cms.entity.LessonResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonResourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    LessonResource toEntity(LessonResourceCreateRequest request);

    LessonResourceResponseDto toResponseDto(LessonResource resource);

    // Update existing entity from create/update request
    void updateEntity(LessonResourceCreateRequest request, @org.mapstruct.MappingTarget LessonResource entity);
}
