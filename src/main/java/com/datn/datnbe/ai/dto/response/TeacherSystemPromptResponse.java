package com.datn.datnbe.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSystemPromptResponse {

    private UUID id;
    private String prompt;
    private boolean isActive;
    private Instant updatedAt;
}
