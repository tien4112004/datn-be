package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.TeacherSystemPromptApi;
import com.datn.datnbe.ai.dto.request.TeacherSystemPromptRequest;
import com.datn.datnbe.ai.dto.response.TeacherSystemPromptResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/teacher/system-prompt")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherSystemPromptController {

    TeacherSystemPromptApi teacherSystemPromptApi;

    @GetMapping
    public ResponseEntity<AppResponseDto<TeacherSystemPromptResponse>> getMyPrompt() {
        TeacherSystemPromptResponse response = teacherSystemPromptApi.getMyPrompt();
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping
    public ResponseEntity<AppResponseDto<TeacherSystemPromptResponse>> upsertMyPrompt(
            @Valid @RequestBody TeacherSystemPromptRequest request) {
        TeacherSystemPromptResponse response = teacherSystemPromptApi.upsertMyPrompt(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyPrompt() {
        teacherSystemPromptApi.deleteMyPrompt();
        return ResponseEntity.noContent().build();
    }
}
