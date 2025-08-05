package com.datn.aiservice.repository.interfaces;

import com.datn.aiservice.entity.ModelConfigurationEntity;

import java.util.List;
import java.util.Optional;

public interface ModelConfigurationRepo {

    boolean existsByModelName(String modelName);

    boolean existsByModelId(Integer modelId);

    ModelConfigurationEntity getModelById(Integer modelId);

    ModelConfigurationEntity getModelByName(String modelName);

    boolean isModelEnabled(Integer modelId);

    List<ModelConfigurationEntity> getModels();

    void save(ModelConfigurationEntity modelEntity);

    void setEnabled(Integer modelId, boolean isEnabled);

    void setDefault(Integer modelId, boolean isDefault);
}
