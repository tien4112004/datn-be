package com.datn.datnbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSystemPromptRequest {

    @NotBlank(message = "Prompt must not be blank")
    @Size(max = 2000, message = "Prompt must not exceed 2000 characters")
    private String prompt;
}
