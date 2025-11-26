package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.document.entity.SlideTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlideTemplateMapper {

    public SlideTemplateResponseDto toResponseDto(SlideTemplate entity) {
        if (entity == null)
            return null;

        Map<String, Object> data = entity.getData() != null ? entity.getData() : new HashMap<>();

        return SlideTemplateResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .layout(entity.getLayout())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .additionalProperties(data)
                .build();
    }
}
