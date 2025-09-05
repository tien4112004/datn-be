package com.datn.datnbe.ai.repository.impl;

import com.datn.datnbe.ai.entity.AIResult;
import com.datn.datnbe.ai.repository.impl.jpa.AIResultJPARepo;
import com.datn.datnbe.ai.repository.interfaces.AIResultRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AIResultRepoImpl implements AIResultRepo {
    AIResultJPARepo AIResultJPARepo;

    @Override
    public AIResult findByPresentationId(String presentationId) {
        return AIResultJPARepo.findByPresentationId(presentationId);
    }

    @Override
    public AIResult save(AIResult aiResult) {
        if (aiResult != null) {
            return AIResultJPARepo.save(aiResult);
        }
        return null;
    }
}
