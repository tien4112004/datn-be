package com.datn.datnbe.document.mapper;

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
}
