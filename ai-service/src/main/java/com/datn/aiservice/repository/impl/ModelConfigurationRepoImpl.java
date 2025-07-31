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
    public void setEnabled(Integer modelId, boolean isEnabled) {
        var existingModel = modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> AppException.builder().errorCode(ErrorCode.MODEL_NOT_FOUND).build());
        existingModel.setEnabled(isEnabled);
        modelConfigurationJPARepo.save(existingModel);
    }

    @Override
    public void setDefault(Integer modelId, boolean isDefault) {
        var existingModel = modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> AppException.builder().errorCode(ErrorCode.MODEL_NOT_FOUND).build());

        if (isDefault && !existingModel.isEnabled()) {
            throw AppException.builder()
                    .errorCode(ErrorCode.MODEL_NOT_ENABLED)
                    .build();
        }

        // Set others models to not default
        if (isDefault) {
            modelConfigurationJPARepo.disableDefaultModelsExcept(modelId);
        }

        existingModel.setDefault(isDefault);
        modelConfigurationJPARepo.save(existingModel);
    }
}
