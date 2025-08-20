package com.datn.datnbe.ai.repository.impl.jpa;


import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ModelConfigurationJPARepo extends JpaRepository<ModelConfigurationEntity, Integer> {
    Optional<ModelConfigurationEntity> findByModelName(String modelName);

    boolean existsByModelName(String modelName);

    @Modifying
    @Transactional
    @Query(value = """
                    UPDATE ModelConfigurationEntity m
                    SET m.isDefault = false
                    WHERE m.isEnabled = true AND m.modelId != :modelId
            """, nativeQuery = true)
    void disableDefaultModelsExcept(Integer modelId);
}
