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

        return models.stream().map(modelDataMapper::toModelMinimalResponseDto).toList();
    }

    @Override
    public ModelResponseDto getModelConfiguration(Integer modelId) {
        var modelEntity = modelConfigurationRepo.getModelById(modelId);

        return modelDataMapper.toModelResponseDto(modelEntity);
    }

    @Override
    public void setModelEnabled(Integer modelId, boolean isEnabled) {
        modelConfigurationRepo.setEnabled(modelId, isEnabled);
    }

    @Override
    @Transactional
    public void setModelDefault(Integer modelId, boolean isDefault) {
        modelConfigurationRepo.setDefault(modelId, isDefault);
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        var modelEntity = modelConfigurationRepo.getModelByName(modelName);

        return modelConfigurationRepo.isModelEnabled(modelEntity.getModelId());
    }

    @Override
    @Transactional
    public void saveModelInfo(ModelInfo modelInfo) {
        var modelEntity = modelDataMapper.toModelConfigurationEntity(modelInfo);
        modelEntity.setEnabled(true);
        modelEntity.setDefault(modelInfo.isDefaultModel());

        modelConfigurationRepo.save(modelEntity);
    }

    @Override
    public boolean existByName(String modelName) {
        return modelConfigurationRepo.existsByModelName(modelName);
    }
}
