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
    @Mapping(target = "subject", ignore = true)
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
        if (type == null) {
            log.warn("Question type is null, cannot convert data");
            return data;
        }

        QuestionType questionType;
        try {
            questionType = QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid question type: {}", type);
            return data;
        }

        switch (questionType) {
            case FILL_IN_BLANK :
                return convertFillInBlankData(data);
            case MULTIPLE_CHOICE :
                return convertMultipleChoiceData(data);
            case MATCHING :
                return convertMatchingData(data);
            case OPEN_ENDED :
                return convertOpenEndedData(data);
            default :
                log.warn("Unhandled question type: {}", questionType);
                return data;
        }
    }

    private Object convertFillInBlankData(Object data) {
        try {
            log.debug("Processing FILL_IN_BLANK data. Data type: {}, Data value: {}",
                    data != null ? data.getClass().getName() : "null",
                    data);

            String textContent = null;
            Boolean caseSensitive = false;

            // Handle multiple formats:
            // 1. com.datn.datnbe.ai.dto.response.FillInBlankData from AI Gateway
            // 2. Map: {type: "FILL_IN_BLANK", data: "text with {{}}", caseSensitive: false}
            // 3. Legacy string: "text with {{}}"

            if (data instanceof com.datn.datnbe.ai.dto.response.FillInBlankData) {
                com.datn.datnbe.ai.dto.response.FillInBlankData aiData = (com.datn.datnbe.ai.dto.response.FillInBlankData) data;
                textContent = aiData.getData();
                caseSensitive = aiData.getCaseSensitive() != null ? aiData.getCaseSensitive() : false;
                log.debug("Processing AI FillInBlankData. Text: {}, CaseSensitive: {}", textContent, caseSensitive);
            } else if (data instanceof String) {
                textContent = (String) data;
                log.debug("Processing FILL_IN_BLANK with direct string format: {}", textContent);
            } else if (data instanceof java.util.Map) {
                @SuppressWarnings("unchecked") java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) data;
                log.debug("Processing FILL_IN_BLANK with Map format. Keys: {}", dataMap.keySet());

                Object nestedData = dataMap.get("data");
                if (nestedData instanceof String) {
                    textContent = (String) nestedData;
                    log.debug("Extracted nested data field: {}", textContent);
                } else {
                    log.warn("Map contains 'data' key but value is not a String. Type: {}, Value: {}",
                            nestedData != null ? nestedData.getClass().getName() : "null",
                            nestedData);
                }

                Object caseSensitiveObj = dataMap.get("caseSensitive");
                if (caseSensitiveObj instanceof Boolean) {
                    caseSensitive = (Boolean) caseSensitiveObj;
                }
            } else {
                log.warn("FILL_IN_BLANK data is neither String, Map, nor AI FillInBlankData. Type: {}",
                        data != null ? data.getClass().getName() : "null");
            }

            if (textContent != null && !textContent.isBlank()) {
                log.debug("Parsing text content with FillInBlankParser: {}", textContent);
                com.datn.datnbe.document.entity.questiondata.FillInBlankData fillInBlankData = FillInBlankParser
                        .parse(textContent);
                fillInBlankData.setCaseSensitive(caseSensitive);
                log.info("Successfully parsed domain FillInBlankData, segments count: {}, caseSensitive: {}",
                        fillInBlankData.getSegments().size(),
                        caseSensitive);
                return fillInBlankData;
            } else {
                log.error("No valid text content found in FILL_IN_BLANK data. Returning original data.");
                return data;
            }
        } catch (Exception e) {
            log.error("Failed to parse FillInBlankData: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertMultipleChoiceData(Object data) {
        try {
            log.debug("Processing MULTIPLE_CHOICE data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.MultipleChoiceData) {
                com.datn.datnbe.ai.dto.response.MultipleChoiceData aiData = (com.datn.datnbe.ai.dto.response.MultipleChoiceData) data;
                java.util.List<com.datn.datnbe.document.entity.questiondata.MultipleChoiceOption> options = aiData
                        .getOptions()
                        .stream()
                        .map(aiOption -> com.datn.datnbe.document.entity.questiondata.MultipleChoiceOption.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .text(aiOption.getText())
                                .imageUrl(aiOption.getImageUrl())
                                .isCorrect(aiOption.getIsCorrect())
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                com.datn.datnbe.document.entity.questiondata.MultipleChoiceData domainData = com.datn.datnbe.document.entity.questiondata.MultipleChoiceData
                        .builder()
                        .options(options)
                        .shuffleOptions(aiData.getShuffleOptions() != null ? aiData.getShuffleOptions() : false)
                        .build();
                log.debug("Converted AI MultipleChoiceData to domain data with {} options", options.size());
                return domainData;
            } else if (data instanceof com.datn.datnbe.document.entity.questiondata.MultipleChoiceData) {
                // Already domain data
                return data;
            }

            log.warn("MULTIPLE_CHOICE data is neither AI nor domain format: {}",
                    data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert MULTIPLE_CHOICE data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertMatchingData(Object data) {
        try {
            log.debug("Processing MATCHING data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.MatchingData) {
                com.datn.datnbe.ai.dto.response.MatchingData aiData = (com.datn.datnbe.ai.dto.response.MatchingData) data;
                java.util.List<com.datn.datnbe.document.entity.questiondata.MatchingPair> pairs = aiData.getPairs()
                        .stream()
                        .map(aiPair -> com.datn.datnbe.document.entity.questiondata.MatchingPair.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .left(aiPair.getLeft())
                                .leftImageUrl(aiPair.getLeftImageUrl())
                                .right(aiPair.getRight())
                                .rightImageUrl(aiPair.getRightImageUrl())
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                com.datn.datnbe.document.entity.questiondata.MatchingData domainData = com.datn.datnbe.document.entity.questiondata.MatchingData
                        .builder()
                        .pairs(pairs)
                        .shufflePairs(aiData.getShufflePairs() != null ? aiData.getShufflePairs() : false)
                        .build();
                log.debug("Converted AI MatchingData to domain data with {} pairs", pairs.size());
                return domainData;
            } else if (data instanceof com.datn.datnbe.document.entity.questiondata.MatchingData) {
                // Already domain data
                return data;
            }

            log.warn("MATCHING data is neither AI nor domain format: {}",
                    data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert MATCHING data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Object convertOpenEndedData(Object data) {
        try {
            log.debug("Processing OPEN_ENDED data. Type: {}", data != null ? data.getClass().getName() : "null");

            if (data instanceof com.datn.datnbe.ai.dto.response.OpenEndedData) {
                com.datn.datnbe.ai.dto.response.OpenEndedData aiData = (com.datn.datnbe.ai.dto.response.OpenEndedData) data;
                com.datn.datnbe.document.entity.questiondata.OpenEndedData domainData = com.datn.datnbe.document.entity.questiondata.OpenEndedData
                        .builder()
                        .expectedAnswer(aiData.getExpectedAnswer())
                        .maxLength(aiData.getMaxLength())
                        .build();
                log.debug("Converted AI OpenEndedData to domain data");
                return domainData;
            } else if (data instanceof com.datn.datnbe.document.entity.questiondata.OpenEndedData) {
                // Already domain data
                return data;
            }

            log.warn("OPEN_ENDED data is neither AI nor domain format: {}",
                    data != null ? data.getClass().getName() : "null");
            return data;
        } catch (Exception e) {
            log.error("Failed to convert OPEN_ENDED data: {}", e.getMessage(), e);
            return data;
        }
    }

    private Class<?> getDataClassForType(QuestionType type) {
        if (type == null) {
            return Object.class;
        }

        switch (type) {
            case OPEN_ENDED :
                return com.datn.datnbe.document.entity.questiondata.OpenEndedData.class;
            case MULTIPLE_CHOICE :
                return com.datn.datnbe.document.entity.questiondata.MultipleChoiceData.class;
            case MATCHING :
                return com.datn.datnbe.document.entity.questiondata.MatchingData.class;
            case FILL_IN_BLANK :
                return com.datn.datnbe.document.entity.questiondata.FillInBlankData.class;
            default :
                log.warn("Unknown question type: {}, defaulting to Object.class", type);
                return Object.class;
        }
    }
}
