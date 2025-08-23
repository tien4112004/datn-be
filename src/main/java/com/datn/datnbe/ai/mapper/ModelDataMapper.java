package com.datn.datnbe.ai.mapper;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.response.ModelMinimalResponseDto;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModelDataMapper {
    @Mapping(target = "isDefault", expression = "java(modelEntity.isDefault())")
    @Mapping(target = "isEnabled", expression = "java(modelEntity.isEnabled())")
    ModelResponseDto toModelResponseDto(ModelConfigurationEntity modelEntity);

    @Mapping(target = "modelId", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "default", ignore = true)
    ModelConfigurationEntity toModelConfigurationEntity(ModelProperties.ModelInfo modelInfo);
}
