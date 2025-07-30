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
    public Optional<ModelConfigurationEntity> getModelById(Integer modelId) {
        return modelConfigurationJPARepo.findById(modelId);
    }

    @Override
    public ModelConfigurationEntity getModelByName(String modelName) {
        return modelConfigurationJPARepo.findByModelName(modelName)
                .orElse(null); // Return null if not found for backward compatibility
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
    public void setEnabled(String modelName, boolean isEnabled) {
        var existingModel = modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(() -> AppException.builder().errorCode(ErrorCode.MODEL_NOT_FOUND).build());
        existingModel.setEnabled(isEnabled);
        modelConfigurationJPARepo.save(existingModel);
    }

    @Override
    public void setDefault(String modelName, boolean isDefault) {
        var existingModel = modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(() -> AppException.builder().errorCode(ErrorCode.MODEL_NOT_FOUND).build());
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
