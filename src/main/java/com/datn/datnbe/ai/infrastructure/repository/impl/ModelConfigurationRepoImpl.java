package com.datn.datnbe.ai.infrastructure.repository.impl;

import com.datn.datnbe.ai.infrastructure.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.infrastructure.repository.impl.jpa.ModelConfigurationJPARepo;
import com.datn.datnbe.ai.infrastructure.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModelConfigurationRepoImpl implements ModelConfigurationRepo {

    ModelConfigurationJPARepo modelConfigurationJPARepo;

    @Override
    public boolean existsByModelName(String modelName) {
        return modelConfigurationJPARepo.existsByModelName(modelName);
    }

    @Override
    public boolean existsByModelId(Integer modelId) {
        return modelConfigurationJPARepo.existsById(modelId);
    }

    @Override
    public ModelConfigurationEntity getModelById(Integer modelId) {
        return modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));
    }

    @Override
    public ModelConfigurationEntity getModelByName(String modelName) {
        return modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(
                        () -> new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found with name: " + modelName));
    }

    @Override
    public boolean isModelEnabled(Integer modelId) {
        return modelConfigurationJPARepo.findById(modelId).map(ModelConfigurationEntity::isEnabled).orElse(false); // Default to false if model not found
    }

    @Override
    public List<ModelConfigurationEntity> getModels() {
        return modelConfigurationJPARepo.findAll();
    }

    @Override
    public void save(ModelConfigurationEntity modelEntity) {
        // Add validation or transformation logic if needed
        log.info("Saving model {}", modelEntity);
        modelConfigurationJPARepo.save(modelEntity);
    }

    @Override
    public void setEnabled(Integer modelId, boolean isEnabled) {
        var existingModel = modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));

        if (!isEnabled && existingModel.isDefault()) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                    "Cannot disable the default model. Please set another model as default first.");
        }

        existingModel.setEnabled(isEnabled);
        modelConfigurationJPARepo.save(existingModel);
    }

    @Override
    public void setDefault(Integer modelId, boolean isDefault) {
        var existingModel = modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));

        if (isDefault && !existingModel.isEnabled()) {
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        // Set others models to not default
        if (isDefault) {
            modelConfigurationJPARepo.disableDefaultModelsExcept(modelId);
        }

        existingModel.setDefault(isDefault);
        modelConfigurationJPARepo.save(existingModel);
    }
}