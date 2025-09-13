package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.response.AIResultResponseDto;

public interface AIResultApi {
    AIResultResponseDto saveAIResult(String aiResult, String presentationId);

    AIResultResponseDto getAIResultByPresentationId(String presentationId);
}
