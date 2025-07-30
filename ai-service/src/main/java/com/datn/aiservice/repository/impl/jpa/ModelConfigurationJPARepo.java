package com.datn.aiservice.repository.impl.jpa;

import com.datn.aiservice.entity.ModelConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelConfigurationJPARepo extends JpaRepository<ModelConfigurationEntity, Integer> {
    Optional<ModelConfigurationEntity> findByModelName(String modelName);

    boolean existsByModelName(String modelName);
}
