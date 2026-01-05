package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.cms.entity.Question;
import com.datn.datnbe.cms.entity.QuestionData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data", expression = "java((com.datn.datnbe.cms.entity.QuestionData) request.getData())")
    Question toEntity(QuestionCreateRequest request);

    QuestionResponseDto toResponseDto(Question entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data", expression = "java(request.getData() != null ? (com.datn.datnbe.cms.entity.QuestionData) request.getData() : entity.getData())")
    void updateEntity(QuestionUpdateRequest request, @MappingTarget Question entity);
}
