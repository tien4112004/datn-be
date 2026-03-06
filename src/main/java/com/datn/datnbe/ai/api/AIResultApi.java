package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.response.AIResultResponseDto;

public interface AIResultApi {
    AIResultResponseDto saveAIResult(String aiResult,
            String presentationId,
            String generationOptions,
            String subject,
            String grade,
            String chapter);

    AIResultResponseDto getAIResultByPresentationId(String presentationId);
}
