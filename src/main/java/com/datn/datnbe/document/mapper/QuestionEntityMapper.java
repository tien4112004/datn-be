package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.QuestionCreateRequest;
import com.datn.datnbe.document.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.questiondata.*;
import com.datn.datnbe.document.utils.FillInBlankParser;
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
    @Mapping(target = "context", ignore = true)
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
        if (data instanceof String && "FILL_IN_BLANK".equalsIgnoreCase(type)) {
            try {
                String textContent = (String) data;
                FillInBlankData fillInBlankData = FillInBlankParser.parse(textContent);
                log.info("Successfully parsed FillInBlankData from string, segments count: {}",
                        fillInBlankData.getSegments().size());
                return fillInBlankData;
            } catch (Exception e) {
                log.error("Failed to parse FillInBlankData from string: {}", e.getMessage(), e);
                return data;
            }
        }

        return data;
    }

    private Class<?> getDataClassForType(QuestionType type) {
        if (type == null) {
            return Object.class;
        }

        switch (type) {
            case OPEN_ENDED :
                return OpenEndedData.class;
            case MULTIPLE_CHOICE :
                return MultipleChoiceData.class;
            case MATCHING :
                return MatchingData.class;
            case FILL_IN_BLANK :
                return FillInBlankData.class;
            default :
                log.warn("Unknown question type: {}, defaulting to Object.class", type);
                return Object.class;
        }
    }
}
