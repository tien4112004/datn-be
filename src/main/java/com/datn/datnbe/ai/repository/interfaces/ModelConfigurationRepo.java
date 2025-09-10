package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;

import java.util.List;

public interface ModelConfigurationRepo {

    /**
     * Checks if a model with the given name exists.
     *
     * @param modelName the name of the model to check
     * @return true if a model with the given name exists, false otherwise
     */
    boolean existsByModelName(String modelName);

    /**
     * Checks if a model with the given ID exists.
     *
     * @param modelId the ID of the model to check
     * @return true if a model with the given ID exists, false otherwise
     */
    boolean existsByModelId(Integer modelId);

    /**
     * Checks if a model with the given name and type exists.
     *
     * @param modelName the name of the model to check
     * @param modelType the type of the model to check
     * @return true if a model with the given name and type exists, false otherwise
     */
    boolean existsByModelNameAndType(String modelName, String modelType);

    /**
     * Checks if a model with the given ID is enabled.
     *
     * @param modelId the ID of the model to check
     * @return true if the model is enabled, false otherwise
     */
    boolean isModelEnabled(Integer modelId);

    /**
     * Retrieves a model by its ID.
     *
     * @param modelId the ID of the model to retrieve
     * @return the ModelConfigurationEntity with the given ID
     */
    ModelConfigurationEntity getModelById(Integer modelId);

    /**
     * Retrieves a model by its name and type.
     *
     * @param modelName the name of the model to retrieve
     * @param modelType the type of the model to retrieve
     * @return the ModelConfigurationEntity with the given name and type
     */
    ModelConfigurationEntity getModelByNameAndType(String modelName, ModelType modelType);

    /**
     * Retrieves all models.
     *
     * @return a list of all ModelConfigurationEntity
     */
    List<ModelConfigurationEntity> getModels();

    /**
     * Retrieves all models of a specific type.
     *
     * @param modelType the type of models to retrieve
     * @return a list of ModelConfigurationEntity of the specified type
     */
    List<ModelConfigurationEntity> getModelsByType(ModelType modelType);

    /**
     * Saves a model entity.
     *
     * @param modelEntity the ModelConfigurationEntity to save
     * @return the saved ModelConfigurationEntity
     */
    ModelConfigurationEntity save(ModelConfigurationEntity modelEntity);

    /**
     * Deletes a model by its name.
     *
     * @param modelName the name of the model to delete
     */
    void deleteByModelName(String modelName);

    /**
     * Sets the enabled status of a model by its ID.
     *
     * @param modelId   the ID of the model to update
     * @param isEnabled the new enabled status
     */
    void setEnabled(Integer modelId, boolean isEnabled);

    /**
     * Sets the default status of a model by its ID. If setting to default, ensures
     * that no other model of the same type is default.
     *
     * @param modelId   the ID of the model to update
     * @param isDefault the new default status
     */
    void setDefault(Integer modelId, boolean isDefault);

    ModelConfigurationEntity getModelByName(String modelName);
}
