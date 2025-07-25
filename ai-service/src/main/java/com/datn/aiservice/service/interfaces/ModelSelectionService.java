package com.datn.aiservice.service.interfaces;

import com.datn.aiservice.dto.response.ModelMinimalResponseDto;
import com.datn.aiservice.dto.response.ModelResponseDto;

public interface ModelSelectionService {

    /**
     * Retrieves the minimal configuration of a model by its ID.
     *
     * @return a list of ModelMinimalResponseDto containing the minimal configuration of the model
     */
    ModelMinimalResponseDto getModelsConfiguration();

    /**
     * Retrieves the full configuration of a model by its ID.
     *
     * @param modelName the ID of the model to retrieve
     * @return a ModelResponseDto containing the full configuration of the model
     */
    ModelResponseDto getFullModelConfiguration(String modelName);

    /**
     * Disables a model by its ID.
     *
     * @param modelName the ID of the model to disable
     */
    void disableModel(String modelName);

    /**
     * Enables a model by its ID.
     *
     * @param modelName the ID of the model to enable
     */
    void setModelEnabled(String modelName, boolean isEnabled);

    /**
     * Sets a model as the default model by its ID.
     *
     * @param v the ID of the model to set as default
     */
    void setDefaultModel(String modelName);

    /**
     * Checks if a model is enabled by its ID.
     *
     * @param modelName the ID of the model to check
     * @return true if the model is enabled, false otherwise
     */
    boolean isModelEnabled(String modelName);
}
