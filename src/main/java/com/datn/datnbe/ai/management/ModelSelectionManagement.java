package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.infrastructure.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.response.ModelMinimalResponseDto;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.infrastructure.mapper.ModelDataMapper;
import com.datn.datnbe.ai.infrastructure.repository.interfaces.ModelConfigurationRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelSelectionManagement implements ModelSelectionApi {
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
    public void saveModelInfo(ModelProperties.ModelInfo modelInfo) {
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
