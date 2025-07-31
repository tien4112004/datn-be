package com.datn.aiservice.service.interfaces;

import java.util.List;

import com.datn.aiservice.config.chatmodelconfiguration.ModelProperties;
import com.datn.aiservice.dto.response.ModelMinimalResponseDto;
import com.datn.aiservice.dto.response.ModelResponseDto;

public interface ModelSelectionService {

    /**
     * Retrieves the minimal configuration of a model by its ID.
     *
     * @return a list of ModelMinimalResponseDto containing the minimal
     * configuration of the model
     */
    List<ModelMinimalResponseDto> getModelConfigurations();

    /**
     * Retrieves the full configuration of a model by its ID.
     *
     * @param modelId the ID of the model to retrieve
     * @return a ModelResponseDto containing the full configuration of the model
     */
    ModelResponseDto getModelConfiguration(Integer modelId);

    /**
     * Enables a model by its ID.
     *
     * @param modelId the ID of the model to enable
     */
    void setModelEnabled(Integer modelId, boolean isEnabled);

    /**
     * Sets a model as the default model by its ID.
     *
     * @param modelId the ID of the model to set as default
     * @param isDefault true if the model should be set as default, false otherwise
     */
    void setModelDefault(Integer modelId, boolean isDefault);

    /**
     * Checks if a model is enabled by its ID.
     *
     * @param modelName the ID of the model to check
     * @return true if the model is enabled, false otherwise
     */
    boolean isModelEnabled(String modelName);

    /**
     * Saves the model information.
     *
     * @param modelInfo the ModelProperties.ModelInfo object containing the model information to save
     */
    void saveModelInfo(ModelProperties.ModelInfo modelInfo);
}
