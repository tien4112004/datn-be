package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.QuestionApi;
import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/api/questionbank")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionController {

    QuestionApi questionApi;
    SecurityContextUtils securityContextUtils;
    ObjectMapper objectMapper;

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
    public ResponseEntity<AppResponseDto<?>> createQuestion(@RequestBody Object requestBody) {
        String currentUserId = securityContextUtils.getCurrentUserId();

        // Check if it's a list (batch) or single object
        if (requestBody instanceof List) {
            try {
                List<QuestionCreateRequest> requests = objectMapper.convertValue(requestBody,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionCreateRequest.class));
                List<QuestionResponseDto> responses = questionApi.createQuestionsBatch(requests, currentUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(responses));
            } catch (Exception e) {
                log.error("Error converting batch request", e);
                log.error("Error details:", e.getCause());
                throw e;
            }
        } else {
            try {
                QuestionCreateRequest request = objectMapper.convertValue(requestBody, QuestionCreateRequest.class);
                QuestionResponseDto response = questionApi.createQuestion(request, currentUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
            } catch (Exception e) {
                log.error("Error converting single question request", e);
                throw e;
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<QuestionResponseDto>> getQuestionById(@PathVariable String id) {
        QuestionResponseDto response = questionApi.getQuestionById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<QuestionResponseDto>> updateQuestion(@PathVariable String id,
            @Valid @RequestBody QuestionUpdateRequest request) {
        QuestionResponseDto response = questionApi.updateQuestion(id, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionApi.deleteQuestion(id);

        return ResponseEntity.noContent().build();
    }
}
