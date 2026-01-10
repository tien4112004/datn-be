package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.cms.entity.QuestionBankItem;
import com.datn.datnbe.cms.entity.questiondata.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class QuestionEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data", ignore = true)
    public abstract QuestionBankItem mapToEntity(QuestionCreateRequest request);

    public QuestionBankItem toEntity(QuestionCreateRequest request) {
        QuestionBankItem question = mapToEntity(request);
        Object convertedData = convertDataToQuestionData(request.getData(), request.getType());
        try {
            // Convert to JSON string for Hibernate to serialize properly
            String jsonData = objectMapper.writeValueAsString(convertedData);
            question.setData(jsonData);
        } catch (Exception e) {
            log.error("Failed to convert data to JSON string", e);
            question.setData(convertedData);
        }
        return question;
    }

    @Mapping(target = "data", ignore = true)
    public abstract QuestionResponseDto mapToResponseDto(QuestionBankItem entity);

    public QuestionResponseDto toResponseDto(QuestionBankItem entity) {
        QuestionResponseDto dto = mapToResponseDto(entity);
        // Deserialize JSON string back to object for response
        if (entity.getData() != null) {
            try {
                if (entity.getData() instanceof String) {
                    Class<?> targetClass = getDataClassForType(entity.getType());
                    Object jsonData = objectMapper.readValue((String) entity.getData(), targetClass);
                    dto.setData(jsonData);
                } else {
                    dto.setData(entity.getData());
                }
            } catch (Exception e) {
                log.error("Failed to deserialize data from JSON string for type: {}", entity.getType(), e);
                dto.setData(entity.getData());
            }
        }
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
            try {
                // Convert to JSON string for Hibernate to serialize properly, consistent with toEntity
                String jsonData = objectMapper.writeValueAsString(request.getData());
                entity.setData(jsonData);
            } catch (Exception e) {
                log.error("Failed to convert data to JSON string", e);
                entity.setData(request.getData());
            }
        }
    }

    private Object convertDataToQuestionData(Object data, String type) {
        // Just return data as-is, let Jackson handle serialization to JSON
        return data;
    }

    private Class<?> getDataClassForType(QuestionType type) {
        if (type == null) {
            return Object.class;
        }
        
        switch (type) {
            case OPEN_ENDED:
                return OpenEndedData.class;
            case MULTIPLE_CHOICE:
                return MultipleChoiceData.class;
            case MATCHING:
                return MatchingData.class;
            case FILL_IN_BLANK:
                return FillInBlankData.class;
            default:
                log.warn("Unknown question type: {}, defaulting to Object.class", type);
                return Object.class;
        }
    }
}
