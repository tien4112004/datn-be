package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.ClassCreateRequest;
import com.datn.datnbe.cms.dto.request.ClassUpdateRequest;
import com.datn.datnbe.cms.dto.response.ClassListResponseDto;
import com.datn.datnbe.cms.dto.response.ClassResponseDto;
import com.datn.datnbe.cms.entity.ClassEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {
        SeatingLayoutMapper.class})
@Named("ClassEntityMapper")
public interface ClassEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentEnrollment", constant = "0")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seatingLayout", ignore = true)
    ClassEntity toEntity(ClassCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentEnrollment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seatingLayout", ignore = true)
    void updateEntity(ClassUpdateRequest request, @MappingTarget ClassEntity entity);

    @Mapping(target = "layout", source = "seatingLayout", qualifiedByName = "toLayoutResponseDto")
    ClassResponseDto toResponseDto(ClassEntity entity);

    ClassListResponseDto toListResponseDto(ClassEntity entity);
}
