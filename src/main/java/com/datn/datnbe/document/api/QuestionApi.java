package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.QuestionCreateRequest;
import com.datn.datnbe.document.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.document.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.document.dto.response.PublishRequestResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionApi {

    PaginatedResponseDto<QuestionResponseDto> getAllQuestions(QuestionCollectionRequest request, String ownerIdFilter);

    PaginatedResponseDto<QuestionResponseDto> getQuestionsByContextId(String contextId,
            QuestionCollectionRequest request);

    QuestionResponseDto createQuestion(QuestionCreateRequest request, String ownerId);

    List<QuestionResponseDto> createQuestionsBatch(List<QuestionCreateRequest> requests, String ownerId);

    BatchCreateQuestionResponseDto createQuestionsBatchWithPartialSuccess(List<QuestionCreateRequest> requests,
            String ownerId);

    QuestionResponseDto getQuestionById(String id);

    QuestionResponseDto updateQuestion(String id, QuestionUpdateRequest request, String userId);

    void deleteQuestion(String id, String userId);

    PaginatedResponseDto<QuestionResponseDto> getPublishedQuestions(Pageable pageable);

    PublishRequestResponseDto publishQuestion(String questionId, String currentUserId);

    PaginatedResponseDto<PublishRequestResponseDto> getPublishRequests(Pageable pageable);

    PublishRequestResponseDto approvePublishRequest(String questionId);
}
