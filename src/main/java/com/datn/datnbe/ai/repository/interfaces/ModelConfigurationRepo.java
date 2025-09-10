package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import java.util.List;

public interface ModelConfigurationRepo {

    boolean existsByModelName(String modelName);

    boolean existsByModelId(Integer modelId);

    ModelConfigurationEntity getModelById(Integer modelId);

    ModelConfigurationEntity getModelByName(String modelName);

    boolean isModelEnabled(Integer modelId);

    List<ModelConfigurationEntity> getModels();

    List<ModelConfigurationEntity> getTextModels();

    List<ModelConfigurationEntity> getImageModels();

    ModelConfigurationEntity save(ModelConfigurationEntity modelEntity);

    void deleteByModelName(String modelName);

    void setEnabled(Integer modelId, boolean isEnabled);

    void setDefault(Integer modelId, boolean isDefault);
}
