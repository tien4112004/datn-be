package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.QuestionApi;
import com.datn.datnbe.cms.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.cms.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.cms.entity.Question;
import com.datn.datnbe.cms.mapper.QuestionEntityMapper;
import com.datn.datnbe.cms.repository.QuestionRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.SmartValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService implements QuestionApi {

    private final QuestionRepository questionRepository;
    private final QuestionEntityMapper questionMapper;
    private final SmartValidator validator;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<QuestionResponseDto> getAllQuestions(QuestionCollectionRequest request,
            String ownerIdFilter) {

        log.info("Fetching questions - bankType: {}, page: {}, pageSize: {}, search: {}, ownerIdFilter: {}",
                request.getBankType(),
                request.getPage(),
                request.getPageSize(),
                request.getSearch(),
                ownerIdFilter);

        if (request.getPage() < 1) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page number must be >= 1");
        }
        if (request.getPageSize() < 1 || request.getPageSize() > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must be between 1 and 100");
        }

        int pageIndex = request.getPage() - 1;

        Sort.Direction direction = Sort.Direction.DESC;
        if (request.getSortDirection() != null) {
            direction = request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize(), direction, sortBy);

        Page<Question> questionPage;

        if (ownerIdFilter != null) {
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                questionPage = questionRepository
                        .findByOwnerIdAndTitleContainingIgnoreCase(ownerIdFilter, request.getSearch(), pageable);
            } else {
                questionPage = questionRepository.findByOwnerId(ownerIdFilter, pageable);
            }
            log.debug("Fetched {} personal questions for user {}", questionPage.getSize(), ownerIdFilter);
        } else {
            // For public bank, get all questions regardless of ownerId
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                questionPage = questionRepository.findByTitleContainingIgnoreCase(request.getSearch(), pageable);
            } else {
                questionPage = questionRepository.findAll(pageable);
            }
            log.debug("Fetched {} public questions", questionPage.getSize());
        }

        List<QuestionResponseDto> dtos = questionPage.getContent()
                .stream()
                .map(questionMapper::toResponseDto)
                .collect(Collectors.toList());

        PaginationDto paginationInfo = PaginationDto.builder()
                .currentPage(request.getPage())
                .pageSize(questionPage.getSize())
                .totalPages(questionPage.getTotalPages())
                .totalItems(questionPage.getTotalElements())
                .build();

        return PaginatedResponseDto.<QuestionResponseDto>builder().data(dtos).pagination(paginationInfo).build();
    }

    @Override
    public QuestionResponseDto createQuestion(QuestionCreateRequest request, String ownerId) {

        log.info("Creating new question - type: {}, title: {}, ownerId: {}",
                request.getType(),
                request.getTitle(),
                ownerId);

        Question question = questionMapper.toEntity(request);
        question.setOwnerId(ownerId);

        Question savedQuestion = questionRepository.save(question);

        log.info("Question created successfully - id: {}, ownerId: {}", savedQuestion.getId(), ownerId);

        return questionMapper.toResponseDto(savedQuestion);
    }

    @Override
    public List<QuestionResponseDto> createQuestionsBatch(List<QuestionCreateRequest> requests, String ownerId) {

        log.info("Creating batch of {} questions - ownerId: {}", requests.size(), ownerId);

        List<Question> questions = requests.stream().map(request -> {
            Question question = questionMapper.toEntity(request);
            question.setOwnerId(ownerId);
            return question;
        }).collect(Collectors.toList());

        List<Question> savedQuestions = questionRepository.saveAll(questions);

        log.info("Batch of {} questions created successfully", savedQuestions.size());

        return savedQuestions.stream().map(questionMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public BatchCreateQuestionResponseDto createQuestionsBatchWithPartialSuccess(List<QuestionCreateRequest> requests,
            String ownerId) {
        log.info("Creating batch of {} questions with partial success handling - ownerId: {}",
                requests.size(),
                ownerId);

        List<QuestionResponseDto> successful = new ArrayList<>();
        List<BatchCreateQuestionResponseDto.BatchItemErrorDto> failed = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            QuestionCreateRequest request = requests.get(i);
            try {
                // Validate the individual request
                org.springframework.validation.BeanPropertyBindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(
                        request, "request");
                validator.validate(request, bindingResult);

                if (bindingResult.hasErrors()) {
                    // Collect field errors
                    Map<String, String> fieldErrors = new HashMap<>();
                    bindingResult.getFieldErrors()
                            .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

                    failed.add(BatchCreateQuestionResponseDto.BatchItemErrorDto.builder()
                            .index(i)
                            .title(request.getTitle())
                            .errorMessage("Validation failed")
                            .fieldErrors(fieldErrors)
                            .build());
                    continue;
                }

                // Create and save the question
                Question question = questionMapper.toEntity(request);
                question.setOwnerId(ownerId);
                Question savedQuestion = questionRepository.save(question);
                successful.add(questionMapper.toResponseDto(savedQuestion));

            } catch (Exception e) {
                log.error("Error processing question at index {} - title: {}", i, request.getTitle(), e);
                failed.add(BatchCreateQuestionResponseDto.BatchItemErrorDto.builder()
                        .index(i)
                        .title(request.getTitle())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        log.info("Batch processed - successful: {}, failed: {}", successful.size(), failed.size());

        return BatchCreateQuestionResponseDto.builder()
                .successful(successful)
                .failed(failed)
                .totalProcessed(requests.size())
                .totalSuccessful(successful.size())
                .totalFailed(failed.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponseDto getQuestionById(String id) {

        log.info("Fetching question - id: {}", id);

        Question question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        return questionMapper.toResponseDto(question);
    }

    @Override
    public QuestionResponseDto updateQuestion(String id, QuestionUpdateRequest request, String userId) {

        log.info("Updating question - id: {}, userId: {}", id, userId);

        Question question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found for update - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        verifyOwnership(question, userId, id);

        questionMapper.updateEntity(request, question);

        Question updatedQuestion = questionRepository.save(question);

        log.info("Question updated successfully - id: {}", id);

        return questionMapper.toResponseDto(updatedQuestion);
    }

    @Override
    public void deleteQuestion(String id, String userId) {

        log.info("Deleting question - id: {}, userId: {}", id, userId);

        Question question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found for deletion - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        verifyOwnership(question, userId, id);

        questionRepository.deleteById(id);

        log.info("Question deleted successfully - id: {}", id);
    }

    private void verifyOwnership(Question question, String userId, String questionId) {
        if (question.getOwnerId() == null || !question.getOwnerId().equals(userId)) {
            log.warn("User {} attempted to modify question {} owned by {}", userId, questionId, question.getOwnerId());
            throw new AppException(ErrorCode.FORBIDDEN, "You do not have permission to modify this question");
        }
    }
}
