package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.AIResult;

import java.util.Optional;

public interface AIResultRepo {
    Optional<AIResult> findByPresentationId(String presentationId);

    AIResult save(AIResult aiResult);
}
