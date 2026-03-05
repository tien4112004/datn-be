package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.TeacherSystemPromptApi;
import com.datn.datnbe.ai.dto.request.TeacherSystemPromptRequest;
import com.datn.datnbe.ai.dto.response.TeacherSystemPromptResponse;
import com.datn.datnbe.ai.entity.TeacherSystemPrompt;
import com.datn.datnbe.ai.repository.TeacherSystemPromptRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherSystemPromptManagement implements TeacherSystemPromptApi {

    TeacherSystemPromptRepository teacherSystemPromptRepository;
    SecurityContextUtils securityContextUtils;

    @Override
    public TeacherSystemPromptResponse getMyPrompt() {
        String teacherId = securityContextUtils.getCurrentUserId();
        TeacherSystemPrompt entity = teacherSystemPromptRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "System prompt not found for teacher: " + teacherId));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public TeacherSystemPromptResponse upsertMyPrompt(TeacherSystemPromptRequest request) {
        String teacherId = securityContextUtils.getCurrentUserId();
        TeacherSystemPrompt entity = teacherSystemPromptRepository.findByTeacherId(teacherId)
                .orElseGet(() -> TeacherSystemPrompt.builder().teacherId(teacherId).build());
        entity.setPrompt(request.getPrompt());
        entity.setActive(true);
        TeacherSystemPrompt saved = teacherSystemPromptRepository.save(entity);
        log.info("[TeacherSystemPrompt] Upserted prompt for teacher={}", teacherId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMyPrompt() {
        String teacherId = securityContextUtils.getCurrentUserId();
        teacherSystemPromptRepository.deleteByTeacherId(teacherId);
        log.info("[TeacherSystemPrompt] Deleted prompt for teacher={}", teacherId);
    }

    private TeacherSystemPromptResponse toResponse(TeacherSystemPrompt entity) {
        return TeacherSystemPromptResponse.builder()
                .id(entity.getId())
                .prompt(entity.getPrompt())
                .isActive(entity.isActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
