package com.datn.datnbe.ai.infrastructure.repository.interfaces;


import com.datn.datnbe.ai.infrastructure.entity.ModelConfigurationEntity;

import java.util.List;

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
