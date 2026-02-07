package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.ModelConfigurationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ModelSelectionManagement implements ModelSelectionApi {
    ModelConfigurationRepository modelConfigurationRepo;
    ModelDataMapper modelDataMapper;

    @Override
    public List<ModelResponseDto> getModelConfigurations() {
        return modelConfigurationRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(ModelConfigurationEntity::getProvider))
                .map(modelDataMapper::toModelResponseDto)
                .toList();
    }

    @Override
    public List<ModelResponseDto> getModelConfigurations(ModelType modelType) {
        if (Objects.isNull(modelType)) {
            return getModelConfigurations();
        } else {
            return modelConfigurationRepo.findAllByModelType(modelType)
                    .stream()
                    .sorted(Comparator.comparing(ModelConfigurationEntity::getProvider))
                    .map(modelDataMapper::toModelResponseDto)
                    .toList();
        }
    }

    @Override
    @Transactional
    public ModelResponseDto setModelStatus(Integer modelId, UpdateModelStatusRequest request) {
        final Boolean isEnabled = request.getIsEnabled();
        final Boolean isDefault = request.getIsDefault();

        // Nothing to update
        if (isEnabled == null && isDefault == null) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                    "At least one of isEnabled or isDefault must be provided");
        }

        // Fetch current model state
        ModelConfigurationEntity modelEntity = modelConfigurationRepo.findById(modelId)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND));

        // If setting default=true, ensure effective enabled state is true
        if (Boolean.TRUE.equals(isDefault)) {
            boolean effectiveEnabled = (isEnabled != null) ? isEnabled : modelEntity.isEnabled();
            if (!effectiveEnabled) {
                throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "A model cannot be default if it is disabled");
            }
        }

        // If the model currently is default, ensure that it cannot be disabled
        if (Boolean.FALSE.equals(isEnabled)) {
            boolean effectiveDefault = (isDefault != null) ? isDefault : modelEntity.isDefault();
            if (effectiveDefault) {
                throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "Cannot disable the default model");
            }
        }

        // Apply updates
        if (isEnabled != null) {
            setEnabled(modelEntity, isEnabled);
        }
        if (isDefault != null) {
            setDefault(modelEntity, isDefault);
        }

        // The entity is managed, but explicit save ensures consistency if new logic is added
        modelConfigurationRepo.save(modelEntity);
        return modelDataMapper.toModelResponseDto(modelEntity);
    }

    private void setEnabled(ModelConfigurationEntity model, boolean isEnabled) {
        if (!isEnabled) {
            // Check if it's the last enabled model of this type
            ModelType modelType = model.getModelType();
            long countEnabled = modelConfigurationRepo.countEnabledModelsByType(modelType);
            if (countEnabled <= 1 && model.isEnabled()) {
                throw new AppException(ErrorCode.INVALID_MODEL_STATUS,
                        "Cannot disable the last enabled model of type: " + modelType.name());
            }
        }
        model.setEnabled(isEnabled);
    }

    private void setDefault(ModelConfigurationEntity model, boolean isDefault) {
        if (!isDefault) {
            throw new AppException(ErrorCode.INVALID_MODEL_STATUS, "Cannot remove default status from model.");
        }

        modelConfigurationRepo.disableDefaultModelsExcept(model.getModelType(), model.getModelId());
        model.setDefault(true);
    }

    @Override
    public boolean isModelEnabled(String modelName) {
        return modelConfigurationRepo.findByModelName(modelName)
                .map(ModelConfigurationEntity::isEnabled)
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found: " + modelName));
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
        return modelConfigurationRepo.existsByModelNameAndModelType(modelName, ModelType.valueOf(modelType));
    }

    @Override
    @Transactional
    public void removeModelByName(String modelName) {
        ModelConfigurationEntity model = modelConfigurationRepo.findByModelName(modelName).orElse(null);

        if (model == null) {
            log.warn("Attempted to remove non-existent model: {}", modelName);
            return;
        }

        // If default, reassign default to another enabled model
        if (model.isDefault()) {
            resetDefaultModel(model.getModelType());
        }

        modelConfigurationRepo.delete(model);
        log.info("Successfully removed model: {}", modelName);
    }

    private void resetDefaultModel(ModelType modelType) {
        modelConfigurationRepo.findAllByModelType(modelType)
                .stream()
                .filter(ModelConfigurationEntity::isEnabled)
                .filter(m -> !m.isDefault()) // Exclude current default (which we are deleting)
                .findFirst()
                .ifPresentOrElse(newDefaultModel -> {
                    newDefaultModel.setDefault(true);
                    modelConfigurationRepo.save(newDefaultModel);
                    log.info("Set model '{}' as the new default.", newDefaultModel.getModelName());
                }, () -> log.warn("Could not find an enabled model to set as the new default after deletion."));
    }
}
