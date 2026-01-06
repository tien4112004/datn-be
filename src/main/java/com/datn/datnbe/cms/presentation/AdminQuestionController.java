package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.QuestionApi;
import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.cms.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
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
@RequestMapping("/api/admin/questionbank")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminQuestionController {

    QuestionApi questionApi;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<QuestionResponseDto>>> getAllPublicQuestions(
            @Valid @ModelAttribute QuestionCollectionRequest request) {

        PaginatedResponseDto<QuestionResponseDto> paginatedResponse = questionApi.getAllQuestions(request, null);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<?>> createPublicQuestion(@RequestBody List<QuestionCreateRequest> requests) {
        BatchCreateQuestionResponseDto response = questionApi.createQuestionsBatchWithPartialSuccess(requests, null);
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

        QuestionResponseDto response = questionApi.updateQuestion(id, request, null);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {

        questionApi.deleteQuestion(id, null);

        return ResponseEntity.noContent().build();
    }
}
