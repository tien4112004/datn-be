package com.datn.datnbe.ai.repository;

import com.datn.datnbe.ai.entity.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIResultRepository extends JpaRepository<AIResult, Integer> {
    Optional<AIResult> findByPresentationId(String presentationId);
}
