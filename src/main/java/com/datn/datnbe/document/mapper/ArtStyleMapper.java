package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.ArtStyleCreateRequest;
import com.datn.datnbe.document.dto.request.ArtStyleUpdateRequest;
import com.datn.datnbe.document.dto.response.ArtStyleResponseDto;
import com.datn.datnbe.document.entity.ArtStyle;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ArtStyleMapper {

    public ArtStyleResponseDto toResponseDto(ArtStyle entity) {
        if (entity == null)
            return null;

        Map<String, Object> data = entity.getData() != null ? entity.getData() : new HashMap<>();

        return ArtStyleResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .labelKey(entity.getLabelKey())
                .visual(entity.getVisual())
                .modifiers(entity.getModifiers())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .additionalProperties(data)
                .build();
    }

    public ArtStyle toEntity(ArtStyleCreateRequest request) {
        if (request == null)
            return null;

        // Note: visual is handled separately in ArtStyleManagement (base64 -> R2 upload)
        return ArtStyle.builder()
                .id(request.getId())
                .name(request.getName())
                .labelKey(request.getLabelKey())
                .modifiers(request.getModifiers())
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .data(request.getData() != null ? request.getData() : new HashMap<>())
                .build();
    }

    public void updateEntity(ArtStyle entity, ArtStyleUpdateRequest request) {
        if (request == null || entity == null)
            return;

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getLabelKey() != null) {
            entity.setLabelKey(request.getLabelKey());
        }
        if (request.getVisual() != null) {
            entity.setVisual(request.getVisual());
        }
        if (request.getModifiers() != null) {
            entity.setModifiers(request.getModifiers());
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

    /**
     * Updates entity fields excluding visual (which is handled separately for base64 upload)
     */
    public void updateEntityExcludingVisual(ArtStyle entity, ArtStyleUpdateRequest request) {
        if (request == null || entity == null)
            return;

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getLabelKey() != null) {
            entity.setLabelKey(request.getLabelKey());
        }
        // Note: visual is handled separately in ArtStyleManagement
        if (request.getModifiers() != null) {
            entity.setModifiers(request.getModifiers());
        }
        if (request.getIsEnabled() != null) {
            entity.setIsEnabled(request.getIsEnabled());
        }
        if (request.getData() != null && !request.getData().isEmpty()) {
            Map<String, Object> mergedData = new HashMap<>(
                    entity.getData() != null ? entity.getData() : new HashMap<>());
            mergedData.putAll(request.getData());
            entity.setData(mergedData);
        }
    }
}
