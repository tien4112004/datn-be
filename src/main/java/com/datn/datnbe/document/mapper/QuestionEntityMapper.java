package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.QuestionCreateRequest;
import com.datn.datnbe.document.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.entity.QuestionBankItem;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Slf4j
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class QuestionEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data", ignore = true)
    public abstract QuestionBankItem mapToEntity(QuestionCreateRequest request);

    public QuestionBankItem toEntity(QuestionCreateRequest request) {
        QuestionBankItem question = mapToEntity(request);
        // With @Type(JsonType.class), Hibernate automatically handles JSON serialization
        // Just set the object directly - no manual conversion needed
        question.setData(request.getData());
        return question;
    }

    @Mapping(target = "data", ignore = true)
    @Mapping(target = "context", ignore = true)
    public abstract QuestionResponseDto mapToResponseDto(QuestionBankItem entity);

    public QuestionResponseDto toResponseDto(QuestionBankItem entity) {
        QuestionResponseDto dto = mapToResponseDto(entity);
        // With @Type(JsonType.class), Hibernate automatically deserializes JSON to objects
        // Just use the data directly - no manual conversion needed
        dto.setData(entity.getData());
        return dto;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data", ignore = true)
    public abstract void mapUpdateEntity(QuestionUpdateRequest request, @MappingTarget QuestionBankItem entity);

    public void updateEntity(QuestionUpdateRequest request, QuestionBankItem entity) {
        mapUpdateEntity(request, entity);
        if (request.getData() != null) {
            // With @Type(JsonType.class), Hibernate automatically handles JSON serialization
            // Just set the object directly - no manual conversion needed
            entity.setData(request.getData());
        }
    }
}
