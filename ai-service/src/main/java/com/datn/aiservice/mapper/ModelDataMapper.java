package com.datn.aiservice.mapper;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties;
import com.datn.aiservice.dto.response.ModelMinimalResponseDto;
import com.datn.aiservice.dto.response.ModelResponseDto;
import com.datn.aiservice.entity.ModelConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModelDataMapper {
    ModelMinimalResponseDto toModelMinimalResponseDto(ModelConfigurationEntity modelConfigurationEntity);

    ModelResponseDto toModelResponseDto(ModelConfigurationEntity modelEntity);

    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "default", ignore = true)
    ModelConfigurationEntity toModelConfigurationEntity(ModelProperties.ModelInfo modelInfo);
}
