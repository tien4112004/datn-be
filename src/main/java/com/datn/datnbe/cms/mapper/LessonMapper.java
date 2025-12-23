package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.LessonCreateRequest;
import com.datn.datnbe.cms.dto.request.LessonUpdateRequest;
import com.datn.datnbe.cms.dto.response.LessonResponseDto;
import com.datn.datnbe.cms.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lesson toEntity(LessonCreateRequest request);

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lesson toEntity(LessonUpdateRequest request);

    LessonResponseDto toResponseDto(Lesson lesson);

    // Update existing entity from update request
    void updateEntity(LessonUpdateRequest request, @org.mapstruct.MappingTarget Lesson entity);
}
