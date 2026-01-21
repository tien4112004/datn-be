package com.datn.datnbe.cms.converter;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter
public class LinkedResourcesConverter implements AttributeConverter<List<LinkedResourceDto>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<LinkedResourceDto> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting LinkedResourceDto list to JSON: {}", e.getMessage());
            throw new RuntimeException("Error converting LinkedResourceDto list to JSON", e);
        }
    }

    @Override
    public List<LinkedResourceDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<LinkedResourceDto>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to LinkedResourceDto list: {}", e.getMessage());
            throw new RuntimeException("Error converting JSON to LinkedResourceDto list", e);
        }
    }
}
