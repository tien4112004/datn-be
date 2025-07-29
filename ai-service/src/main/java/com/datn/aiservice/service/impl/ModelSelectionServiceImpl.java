package com.datn.aiservice.service.impl;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties.ModelInfo;
import com.datn.aiservice.dto.response.ModelMinimalResponseDto;
import com.datn.aiservice.dto.response.ModelResponseDto;
import com.datn.aiservice.mapper.ModelDataMapper;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import com.datn.aiservice.service.interfaces.ModelSelectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelSelectionServiceImpl implements ModelSelectionService {
    ModelConfigurationRepo modelConfigurationRepo;
    ModelDataMapper modelDataMapper;

    @Override
    public List<ModelMinimalResponseDto> getModelConfigurations() {
        var models = modelConfigurationRepo.getModels();

        return models.stream()
                .map(modelDataMapper::toModelMinimalResponseDto)
                .toList();
    }

    @Override
    public ModelResponseDto getModelConfiguration(String modelName) {
        var modelEntity = modelConfigurationRepo.getModelByName(modelName);
        return modelDataMapper.toModelResponseDto(modelEntity);
    }

    @Override
    public void setModelEnabled(String modelName, boolean isEnabled) {
        modelConfigurationRepo.setEnabled(modelName, isEnabled);
    }

    @Override
    @Transactional
    public void setDefault(String modelName, boolean isDefault) {
        modelConfigurationRepo.setDefault(modelName, isDefault);
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        return modelConfigurationRepo.isModelEnabled(modelName);
    }

    @Override
    @Transactional
    public void saveModelInfo(ModelInfo modelInfo) {
        var modelEntity = modelDataMapper.toModelConfigurationEntity(modelInfo);

        modelConfigurationRepo.save(modelEntity);
    }
}
