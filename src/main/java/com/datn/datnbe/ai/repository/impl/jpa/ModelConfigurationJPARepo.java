package com.datn.datnbe.ai.repository.impl.jpa;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ModelConfigurationJPARepo extends JpaRepository<ModelConfigurationEntity, Integer> {
    Optional<ModelConfigurationEntity> findByModelName(String modelName);

    boolean existsByModelName(String modelName);

    @Modifying
    @Transactional
    @Query(value = """
                    UPDATE model_configuration AS m
                    SET is_default = false
                    WHERE m.is_enabled = true AND m.model_type = :modelType AND m.id != :modelId
            """, nativeQuery = true)
    void disableDefaultModelsExcept(@Param("modelType") String modelType, @Param("modelId") Integer modelId);

    @Query(value = """
                    SELECT count(*)
                    FROM model_configuration AS m
                    WHERE m.model_type = :modelType AND m.is_enabled = true
            """, nativeQuery = true)
    long countEnabledModelsByType(@Param("modelType") String modelType);

    boolean existsByModelNameAndModelType(String modelName, ModelType modelType);

    Optional<ModelConfigurationEntity> findByModelNameAndModelType(String modelName, ModelType modelType);

    List<ModelConfigurationEntity> findAllByModelType(ModelType modelType);
}
