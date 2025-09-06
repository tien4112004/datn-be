package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import java.util.List;

public interface ModelSelectionApi {

    /**
     * Retrieves the minimal configuration of a model by its ID.
     *
     * @return a list of ModelResponseDto containing the minimal configuration of the model
     */
    List<ModelResponseDto> getModelConfigurations();

    /**
     * Enables a model by its ID.
     *
     * @param modelId the ID of the model to enable
     * @param request the request containing the status update information
     * @return a ModelResponseDto containing the updated model information
     */
    ModelResponseDto setModelStatus(Integer modelId, UpdateModelStatusRequest request);

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

    /**
     * Checks if a model exists by its name.
     *
     * @param modelName the name of the model to check
     * @return true if the model exists, false otherwise
     */
    boolean existByName(String modelName);

    /**
     * Removes a model by its name. This method is only used internally
     *
     * @param modelName the name of the model to remove
     */
    void removeModelByName(String modelName);
}
