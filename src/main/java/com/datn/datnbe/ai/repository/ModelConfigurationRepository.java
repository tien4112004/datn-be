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

    @Query("SELECT COUNT(m) FROM ModelConfigurationEntity m WHERE m.modelType = :modelType AND m.isEnabled = true")
    long countEnabledModelsByType(@Param("modelType") String modelType);

    @Modifying
    @Query("UPDATE ModelConfigurationEntity m SET m.isDefault = false WHERE m.modelType = :modelType AND m.id <> :modelId")
    void disableDefaultModelsExcept(@Param("modelType") String modelType, @Param("modelId") Integer modelId);
}
