package com.datn.datnbe.ai.repository.impl;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.repository.impl.jpa.ModelConfigurationJPARepo;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
        return modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));
    }

    @Override
    public ModelConfigurationEntity getModelByNameAndType(String modelName, ModelType modelType) {
        return modelConfigurationJPARepo.findByModelNameAndModelType(modelName, modelType)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND,
                        "Model not found with name: " + modelName + " and type: " + modelType));
    }

    @Override
    public boolean isModelEnabled(Integer modelId) {
        return modelConfigurationJPARepo.findById(modelId).map(ModelConfigurationEntity::isEnabled).orElse(false);
    }

    @Override
    public List<ModelConfigurationEntity> getModels() {
        return modelConfigurationJPARepo.findAll();
    }

    @Override
    public ModelConfigurationEntity save(ModelConfigurationEntity modelEntity) {
        log.info("Saving model {}", modelEntity);
        return modelConfigurationJPARepo.save(modelEntity);
    }

    @Override
    public void setEnabled(Integer modelId, boolean isEnabled) {
        var existingModel = modelConfigurationJPARepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));

        if (!isEnabled && existingModel.isDefault()) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                    "Cannot disable a default model, please set another model as default first");
        }

        var modelType = existingModel.getModelType().name();
        var countEnabledModelsOfType = modelConfigurationJPARepo.countEnabledModelsByType(modelType);

        if (!isEnabled && countEnabledModelsOfType <= 1) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                    "Cannot disable the last enabled model of type: " + modelType);
        }

        existingModel.setEnabled(isEnabled);
        modelConfigurationJPARepo.save(existingModel);
    }

    @Override
    public void setDefault(Integer modelId, boolean isDefault) {
        ModelConfigurationEntity model = getModelById(modelId);

        if (isDefault && model.isEnabled()) {
            // First disable all other default models of the same type
            modelConfigurationJPARepo.disableDefaultModelsExcept(model.getModelType().name(), modelId);

            // Then set this model as default
            model.setDefault(true);
            modelConfigurationJPARepo.save(model);
        } else if (!isDefault) {
            model.setDefault(false);
            modelConfigurationJPARepo.save(model);
        } else {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "Cannot set a disabled model as default.");
        }
    }

    @Override
    public ModelConfigurationEntity getModelByName(String modelName) {
        return modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(
                        () -> new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found with name: " + modelName));
    }

    @Override
    public boolean existsByModelNameAndType(String modelName, String modelType) {
        return modelConfigurationJPARepo.existsByModelNameAndModelType(modelName, ModelType.valueOf(modelType));
    }

    @Override
    public void deleteByModelName(String modelName) {
        var model = modelConfigurationJPARepo.findByModelName(modelName)
                .orElseThrow(
                        () -> new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found with name: " + modelName));

        // Set other model as default if the deleted model is default
        if (model.isDefault()) {
            resetDefaultModel();
        }

        modelConfigurationJPARepo.delete(model);
        log.info("Deleted model: {}", modelName);
    }

    private void resetDefaultModel() {
        modelConfigurationJPARepo.findAll()
                .stream()
                .filter(ModelConfigurationEntity::isEnabled)
                .findFirst()
                .ifPresentOrElse(newDefaultModel -> {
                    newDefaultModel.setDefault(true);
                    modelConfigurationJPARepo.save(newDefaultModel);
                    log.info("Set model '{}' as the new default.", newDefaultModel.getModelName());
                }, () -> log.warn("Could not find an enabled model to set as the new default after deletion."));
    }

    @Override
    public List<ModelConfigurationEntity> getModelsByType(ModelType modelType) {
        return modelConfigurationJPARepo.findAllByModelType(modelType);
    }
}
