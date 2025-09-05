package com.datn.datnbe.ai.repository.impl.jpa;

import com.datn.datnbe.ai.entity.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIResultJPARepo extends JpaRepository<AIResult, Integer> {
    AIResult findByPresentationId(String presentationId);
}
