package com.datn.aiservice.repository.interfaces;

import com.datn.aiservice.entity.ModelConfigurationEntity;

import java.util.List;
import java.util.Optional;

public interface ModelConfigurationRepo {

    String getModelName();

    Optional<ModelConfigurationEntity> getModelById(Integer modelId);

    ModelConfigurationEntity getModelByName(String modelName);

    boolean isModelEnabled(String modelName);

    List<ModelConfigurationEntity> getModels();

    void save(ModelConfigurationEntity modelEntity);

    void setEnabled(String modelName, boolean isEnabled);

    void setDefault(String modelName, boolean isDefault);
}
