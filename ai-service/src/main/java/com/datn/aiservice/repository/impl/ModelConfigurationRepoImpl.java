package com.datn.aiservice.repository.impl;

import com.datn.aiservice.entity.ModelConfigurationEntity;
import com.datn.aiservice.exceptions.AppException;
import com.datn.aiservice.exceptions.ErrorCode;
import com.datn.aiservice.repository.impl.jpa.ModelConfigurationJPARepo;
import com.datn.aiservice.repository.interfaces.ModelConfigurationRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        return modelConfigurationJPARepo.findById(modelId).orElseThrow(
                () -> new AppException(ErrorCode.MODEL_NOT_FOUND)
        );
    }

    @Override
    public ModelConfigurationEntity getModelByName(String modelName) {
        return modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND,
                        "Model not found with name: " + modelName));
    }

    @Override
    public boolean isModelEnabled(Integer modelId) {
        return modelConfigurationJPARepo.findById(modelId)
                .map(ModelConfigurationEntity::isEnabled)
                .orElse(false); // Default to false if model not found
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

    @Override
    public void setAllModelsNotDefaultExcept(String modelName) {
        var models = modelConfigurationJPARepo.findAll();

        models.stream()
            .filter(model -> !model.getModelName().equals(modelName))
            .forEach(model -> {
                model.setDefault(false);
                modelConfigurationJPARepo.save(model);
            });
    }
}
