package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.SlideThemeCreateRequest;
import com.datn.datnbe.document.dto.request.SlideThemeUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.document.entity.SlideTheme;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlideThemeMapper {

    public SlideThemeResponseDto toResponseDto(SlideTheme entity) {
        if (entity == null)
            return null;

        Map<String, Object> data = entity.getData() != null ? entity.getData() : new HashMap<>();

        return SlideThemeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .additionalProperties(data)
                .build();
    }

    public SlideTheme toEntity(SlideThemeCreateRequest request) {
        if (request == null)
            return null;

        return SlideTheme.builder()
                .id(request.getId())
                .name(request.getName())
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .data(request.getData() != null ? request.getData() : new HashMap<>())
                .build();
    }

    public void updateEntity(SlideTheme entity, SlideThemeUpdateRequest request) {
        if (request == null || entity == null)
            return;

        if (request.getName() != null) {
            entity.setName(request.getName());
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
