package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelConfigurationRepository extends JpaRepository<ModelConfigurationEntity, Integer> {

    boolean existsByModelName(String modelName);

    void deleteByModelName(String modelName);

    Optional<ModelConfigurationEntity> findByModelNameAndModelType(String modelName, ModelType modelType);

    boolean existsByModelNameAndModelType(String modelName, ModelType modelType);

    Optional<ModelConfigurationEntity> findByModelName(String modelName);

    List<ModelConfigurationEntity> findAllByModelType(ModelType modelType);

    @Query("SELECT COUNT(m) FROM model_configuration m WHERE m.modelType = :modelType AND m.isEnabled = true")
    long countEnabledModelsByType(@Param("modelType") ModelType modelType);

    @Modifying
    @Query("UPDATE model_configuration m SET m.isDefault = false WHERE m.modelType = :modelType AND m.modelId <> :modelId")
    void disableDefaultModelsExcept(@Param("modelType") ModelType modelType, @Param("modelId") Integer modelId);
}
