package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.cms.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.cms.dto.response.AssignmentResponse;
import com.datn.datnbe.cms.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assignment toEntity(AssignmentCreateRequest request);

    AssignmentResponse toDto(Assignment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Assignment entity, AssignmentUpdateRequest request);
}
