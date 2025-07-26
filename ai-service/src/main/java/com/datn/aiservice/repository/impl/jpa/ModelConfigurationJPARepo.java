package com.datn.aiservice.repository.impl.jpa;

import com.datn.aiservice.entity.ModelConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelConfigurationJPARepo extends JpaRepository<ModelConfigurationEntity, Integer> {
}
