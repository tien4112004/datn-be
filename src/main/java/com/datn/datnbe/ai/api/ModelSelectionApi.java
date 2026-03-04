package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.CreateModelRequest;
import com.datn.datnbe.ai.dto.request.UpdateModelRequest;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.enums.ModelType;

import java.util.List;

public interface ModelSelectionApi {

    /**
     * Retrieves all active model configurations.
     *
     * @return a list of ModelResponseDto
     */
    List<ModelResponseDto> getModelConfigurations();

    /**
     * Retrieves model configurations, optionally including soft-deleted ones.
     *
     * @param modelType      the type of the model to retrieve (nullable for all types)
     * @param includeDeleted whether to include soft-deleted models
     * @return a list of ModelResponseDto
     */
    List<ModelResponseDto> getModelConfigurations(ModelType modelType, boolean includeDeleted);

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
     *
     * @return true if the model is enabled, false otherwise
     */
    boolean isModelEnabled(String modelName);

    /**
     * Saves the model information.
     *
     * @param modelInfo the ModelProperties.ModelInfo object containing the model
     *                  information to save
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
     * Checks if a model exists by its name and type.
     *
     * @param modelName the name of the model to check
     * @param modelType the type of the model to check
     * @return true if the model exists, false otherwise
     */
    boolean existByNameAndType(String modelName, String modelType);

    /**
     * Removes a model by its name. This method is only used internally
     *
     * @param modelName the name of the model to remove
     */
    void removeModelByName(String modelName);

    /**
     * Creates a new model configuration.
     *
     * @param request the request containing the model information to create
     * @return a ModelResponseDto containing the created model information
     */
    ModelResponseDto createModel(CreateModelRequest request);

    /**
     * Deletes a model configuration by its ID.
     *
     * @param modelId the ID of the model to delete
     */
    void deleteModel(Integer modelId);

    /**
     * Updates the name, displayName and provider of an existing model.
     *
     * @param modelId the ID of the model to update
     * @param request the request containing updated model information
     * @return a ModelResponseDto containing the updated model information
     */
    ModelResponseDto updateModel(Integer modelId, UpdateModelRequest request);
}
