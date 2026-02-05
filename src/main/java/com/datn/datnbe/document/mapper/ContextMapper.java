package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.ContextCreateRequest;
import com.datn.datnbe.document.dto.request.ContextUpdateRequest;
import com.datn.datnbe.document.dto.response.ContextResponse;
import com.datn.datnbe.document.entity.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContextMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromBook", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Context toEntity(ContextCreateRequest request);

    ContextResponse toDto(Context entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromBook", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Context entity, ContextUpdateRequest request);
}
