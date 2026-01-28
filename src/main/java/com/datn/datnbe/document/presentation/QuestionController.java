package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.QuestionApi;
import com.datn.datnbe.document.dto.request.QuestionCreateRequest;
import com.datn.datnbe.document.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.document.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionController {

    QuestionApi questionApi;
    SecurityContextUtils securityContextUtils;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<QuestionResponseDto>>> getAllQuestions(
            @Valid @ModelAttribute QuestionCollectionRequest request) {
        String currentUserId = null;
        if ("personal".equalsIgnoreCase(request.getBankType())) {
            currentUserId = securityContextUtils.getCurrentUserId();
        }

        PaginatedResponseDto<QuestionResponseDto> paginatedResponse = questionApi.getAllQuestions(request,
                currentUserId);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<?>> createQuestion(@RequestBody List<QuestionCreateRequest> requests) {
        String currentUserId = securityContextUtils.getCurrentUserId();
        BatchCreateQuestionResponseDto response = questionApi.createQuestionsBatchWithPartialSuccess(requests,
                currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<QuestionResponseDto>> getQuestionById(@PathVariable String id) {
        QuestionResponseDto response = questionApi.getQuestionById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<QuestionResponseDto>> updateQuestion(@PathVariable String id,
            @Valid @RequestBody QuestionUpdateRequest request) {
        String currentUserId = securityContextUtils.getCurrentUserId();
        QuestionResponseDto response = questionApi.updateQuestion(id, request, currentUserId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        String currentUserId = securityContextUtils.getCurrentUserId();
        questionApi.deleteQuestion(id, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contextId}/questions")
    public ResponseEntity<AppResponseDto<List<QuestionResponseDto>>> getQuestionsByContextId(
            @PathVariable String contextId,
            @Valid @ModelAttribute QuestionCollectionRequest request) {
        log.info("Fetching all questions for contextId: {}", contextId);

        PaginatedResponseDto<QuestionResponseDto> paginatedResponse = questionApi.getQuestionsByContextId(contextId,
                request);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }
}
