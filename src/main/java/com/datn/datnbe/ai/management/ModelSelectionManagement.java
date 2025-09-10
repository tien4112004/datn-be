package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        return modelConfigurationRepo.getModels()
                .stream()
                .sorted(Comparator.comparing(ModelConfigurationEntity::getModelName))
                .map(modelDataMapper::toModelResponseDto)
                .toList();
    }

    @Override
    public List<ModelResponseDto> getModelConfigurations(ModelType modelType) {

        return modelConfigurationRepo.getModelsByType(modelType)
                .stream()
                .sorted(Comparator.comparing(ModelConfigurationEntity::getModelName))
                .map(modelDataMapper::toModelResponseDto)
                .toList();
    }

    @Override
    public ModelResponseDto setModelStatus(Integer modelId, UpdateModelStatusRequest request) {
        if (!modelConfigurationRepo.existsByModelId(modelId)) {
            throw new AppException(ErrorCode.MODEL_NOT_FOUND, "Model with ID " + modelId + " does not exist");
        }

        final Boolean isEnabled = request.getIsEnable();
        final Boolean isDefault = request.getIsDefault();

        // Nothing to update
        if (isEnabled == null && isDefault == null) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                    "At least one of isEnabled or isDefault must be provided");
        }

        // If setting default=true, ensure effective enabled state is true
        if (Boolean.TRUE.equals(isDefault)) {
            boolean effectiveEnabled = (isEnabled != null) ? isEnabled : modelConfigurationRepo.isModelEnabled(modelId);
            if (!effectiveEnabled) {
                throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "A model cannot be default if it is disabled");
            }
        }

        // Apply updates (each independently if present)
        if (isEnabled != null) {
            modelConfigurationRepo.setEnabled(modelId, isEnabled);
        }
        if (isDefault != null) {
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
    public boolean existByNameAndType(String modelName, String modelType) {
        return modelConfigurationRepo.existsByModelNameAndType(modelName, modelType);
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
