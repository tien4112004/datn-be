package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
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

    public SlideTemplate toEntity(SlideTemplateCreateRequest request) {
        if (request == null)
            return null;

        return SlideTemplate.builder()
                .id(request.getId())
                .name(request.getName())
                .layout(request.getLayout())
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .data(request.getData() != null ? request.getData() : new HashMap<>())
                .build();
    }

    public void updateEntity(SlideTemplate entity, SlideTemplateUpdateRequest request) {
        if (request == null || entity == null)
            return;

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getLayout() != null) {
            entity.setLayout(request.getLayout());
        }
        if (request.getIsEnabled() != null) {
            entity.setIsEnabled(request.getIsEnabled());
        }
        if (request.getData() != null && !request.getData().isEmpty()) {
            // Merge existing data with new data
            Map<String, Object> mergedData = new HashMap<>(
                    entity.getData() != null ? entity.getData() : new HashMap<>());
            mergedData.putAll(request.getData());
            entity.setData(mergedData);
        }
    }
}
