package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelSelectionManagement implements ModelSelectionApi {
    ModelConfigurationRepo modelConfigurationRepo;
    ModelDataMapper modelDataMapper;

    @Override
    public List<ModelResponseDto> getModelConfigurations() {
        var models = modelConfigurationRepo.getModels();

        return models.stream().map(modelDataMapper::toModelResponseDto).toList();
    }

    @Override
    public ModelResponseDto getModelConfiguration(Integer modelId) {
        var modelEntity = modelConfigurationRepo.getModelById(modelId);

        return modelDataMapper.toModelResponseDto(modelEntity);
    }

    @Override
    public ModelResponseDto setModelStatus(Integer modelId, Boolean isEnabled, Boolean isDefault) {
        if (isEnabled == null && isDefault == null) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "At least one of isEnabled or isDefault must be provided");
        }

        if (isEnabled != null && isDefault == null) {
            modelConfigurationRepo.setEnabled(modelId, isEnabled);
        }

        if (isDefault != null && isEnabled == null) {
            modelConfigurationRepo.setDefault(modelId, isDefault);
        }

        if (isEnabled != null && isDefault != null) {
            if (!isEnabled && isDefault) {
                throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "A model cannot be default if it is disabled");
            }

            modelConfigurationRepo.setEnabled(modelId, isEnabled);
            modelConfigurationRepo.setDefault(modelId, isDefault);
        }

        var modelEntity = modelConfigurationRepo.getModelById(modelId);

        return modelDataMapper.toModelResponseDto(modelEntity);
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

    @Override
    @Transactional
    public void removeModelByName(String modelName) {
        if (modelConfigurationRepo.existsByModelName(modelName)) {
            modelConfigurationRepo.deleteByModelName(modelName);
            log.info("Successfully removed model: {}", modelName);
        } else {
            log.warn("Attempted to remove non-existent model: {}", modelName);
        }
    }
}
