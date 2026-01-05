package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

import java.util.List;

public interface QuestionApi {

    PaginatedResponseDto<QuestionResponseDto> getAllQuestions(QuestionCollectionRequest request, String ownerIdFilter);

    QuestionResponseDto createQuestion(QuestionCreateRequest request, String ownerId);

    List<QuestionResponseDto> createQuestionsBatch(List<QuestionCreateRequest> requests, String ownerId);

    QuestionResponseDto getQuestionById(String id);

    QuestionResponseDto updateQuestion(String id, QuestionUpdateRequest request);

    void deleteQuestion(String id);
}
