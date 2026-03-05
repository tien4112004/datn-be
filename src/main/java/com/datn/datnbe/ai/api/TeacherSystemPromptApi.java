package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.request.TeacherSystemPromptRequest;
import com.datn.datnbe.ai.dto.response.TeacherSystemPromptResponse;

public interface TeacherSystemPromptApi {

    TeacherSystemPromptResponse getMyPrompt();

    TeacherSystemPromptResponse upsertMyPrompt(TeacherSystemPromptRequest request);

    void deleteMyPrompt();
}
