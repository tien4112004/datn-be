package com.datn.datnbe.ai.repository.interfaces;

import com.datn.datnbe.ai.entity.AIResult;

public interface AIResultRepo {
    AIResult findByPresentationId(String presentationId);

    AIResult save(AIResult aiResult);
}
