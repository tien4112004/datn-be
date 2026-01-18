package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.QuestionApi;
import com.datn.datnbe.document.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.document.dto.request.QuestionCreateRequest;
import com.datn.datnbe.document.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.document.dto.response.PublishRequestResponseDto;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.PublishRequest;
import com.datn.datnbe.document.mapper.QuestionEntityMapper;
import com.datn.datnbe.document.repository.QuestionRepository;
import com.datn.datnbe.document.repository.PublishRequestRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.SmartValidator;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionManagement implements QuestionApi {

    private final QuestionRepository questionRepository;
    private final PublishRequestRepository publishRequestRepository;
    private final QuestionEntityMapper questionMapper;
    private final SmartValidator validator;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<QuestionResponseDto> getAllQuestions(QuestionCollectionRequest request,
            String ownerIdFilter) {

        log.info(
                "Fetching questions - bankType: {}, page: {}, pageSize: {}, search: {}, difficulty: {}, type: {}, subject: {}, grade: {}, chapter: {}, ownerIdFilter: {}",
                request.getBankType(),
                request.getPage(),
                request.getPageSize(),
                request.getSearch(),
                request.getDifficulty(),
                request.getType(),
                request.getSubject(),
                request.getGrade(),
                request.getChapter(),
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

        // Build specifications for filtering
        Specification<QuestionBankItem> spec = buildSpecification(request, ownerIdFilter);

        Page<QuestionBankItem> questionPage = questionRepository.findAll(spec, pageable);

        log.debug("Fetched {} questions", questionPage.getSize());

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

        QuestionBankItem question = questionMapper.toEntity(request);
        question.setOwnerId(ownerId);

        QuestionBankItem savedQuestion = questionRepository.save(question);

        log.info("Question created successfully - id: {}, ownerId: {}", savedQuestion.getId(), ownerId);

        return questionMapper.toResponseDto(savedQuestion);
    }

    @Override
    public List<QuestionResponseDto> createQuestionsBatch(List<QuestionCreateRequest> requests, String ownerId) {

        log.info("Creating batch of {} questions - ownerId: {}", requests.size(), ownerId);

        List<QuestionBankItem> questions = requests.stream().map(request -> {
            QuestionBankItem question = questionMapper.toEntity(request);
            question.setOwnerId(ownerId);
            return question;
        }).collect(Collectors.toList());

        List<QuestionBankItem> savedQuestions = questionRepository.saveAll(questions);

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
                QuestionBankItem question = questionMapper.toEntity(request);
                question.setOwnerId(ownerId);
                QuestionBankItem savedQuestion = questionRepository.save(question);
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

        // If no items were successfully created, throw an exception with the first error
        if (successful.isEmpty() && !failed.isEmpty()) {
            BatchCreateQuestionResponseDto.BatchItemErrorDto firstError = failed.get(0);
            log.error("Batch creation failed completely. First error - index: {}, title: {}, message: {}",
                    firstError.getIndex(),
                    firstError.getTitle(),
                    firstError.getErrorMessage());
            throw new AppException(ErrorCode.VALIDATION_ERROR, firstError.getErrorMessage());
        }

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

        QuestionBankItem question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        return questionMapper.toResponseDto(question);
    }

    @Override
    public QuestionResponseDto updateQuestion(String id, QuestionUpdateRequest request, String userId) {

        log.info("Updating question - id: {}, userId: {}", id, userId);

        QuestionBankItem question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found for update - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        verifyOwnership(question, userId, id);

        questionMapper.updateEntity(request, question);

        QuestionBankItem updatedQuestion = questionRepository.save(question);

        log.info("Question updated successfully - id: {}", id);

        return questionMapper.toResponseDto(updatedQuestion);
    }

    @Override
    public void deleteQuestion(String id, String userId) {

        log.info("Deleting question - id: {}, userId: {}", id, userId);

        QuestionBankItem question = questionRepository.findById(id).orElseThrow(() -> {
            log.warn("Question not found for deletion - id: {}", id);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + id);
        });

        verifyOwnership(question, userId, id);

        questionRepository.deleteById(id);

        log.info("Question deleted successfully - id: {}", id);
    }

    private void verifyOwnership(QuestionBankItem question, String userId, String questionId) {
        if (question.getOwnerId() == null || !question.getOwnerId().equals(userId)) {
            log.warn("User {} attempted to modify question {} owned by {}", userId, questionId, question.getOwnerId());
            throw new AppException(ErrorCode.FORBIDDEN, "You do not have permission to modify this question");
        }
    }

    private Specification<QuestionBankItem> buildSpecification(QuestionCollectionRequest request,
            String ownerIdFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Owner filter
            if (ownerIdFilter != null) {
                predicates.add(cb.equal(root.get("ownerId"), ownerIdFilter));
            }

            // Search by title
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + request.getSearch().toLowerCase() + "%"));
            }

            // Filter by difficulty (supports multi-select)
            if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
                predicates.add(root.get("difficulty").in(request.getDifficulty()));
            }

            // Filter by type (supports multi-select)
            if (request.getType() != null && !request.getType().isEmpty()) {
                predicates.add(root.get("type").in(request.getType()));
            }

            // Filter by subject (supports multi-select)
            if (request.getSubject() != null && !request.getSubject().isEmpty()) {
                predicates.add(root.get("subject").in(request.getSubject()));
            }

            // Filter by grade (supports multi-select)
            if (request.getGrade() != null && !request.getGrade().isEmpty()) {
                predicates.add(root.get("grade").in(request.getGrade()));
            }

            // Filter by chapter (supports multi-select)
            if (request.getChapter() != null && !request.getChapter().isEmpty()) {
                predicates.add(root.get("chapter").in(request.getChapter()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<QuestionResponseDto> getPublishedQuestions(Pageable pageable) {
        log.info("Fetching published questions");

        Page<QuestionBankItem> publishedQuestions = questionRepository.findByOwnerIdIsNull(pageable);

        List<QuestionResponseDto> dtos = publishedQuestions.getContent()
                .stream()
                .map(questionMapper::toResponseDto)
                .collect(Collectors.toList());

        PaginationDto paginationInfo = PaginationDto.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(publishedQuestions.getSize())
                .totalPages(publishedQuestions.getTotalPages())
                .totalItems(publishedQuestions.getTotalElements())
                .build();

        return PaginatedResponseDto.<QuestionResponseDto>builder().data(dtos).pagination(paginationInfo).build();
    }

    @Transactional
    public PublishRequestResponseDto publishQuestion(String questionId, String currentUserId) {
        log.info("Publishing question - questionId: {}, userId: {}", questionId, currentUserId);

        QuestionBankItem question = questionRepository.findById(questionId).orElseThrow(() -> {
            log.warn("Question not found for publishing - id: {}", questionId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + questionId);
        });

        // Check if user already owns this question
        if (question.getOwnerId() != null && question.getOwnerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "You cannot publish your own question");
        }

        // Check if a pending/approved request already exists
        if (publishRequestRepository.existsByQuestionIdAndRequesterIdAndIsDeletedFalse(questionId, currentUserId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "You already have an active publish request for this question");
        }

        // Create publish request
        PublishRequest publishRequest = PublishRequest.builder()
                .questionId(questionId)
                .requesterId(currentUserId)
                .status(PublishRequest.PublishRequestStatus.PENDING)
                .isDeleted(false)
                .build();

        PublishRequest savedRequest = publishRequestRepository.save(publishRequest);

        log.info("Publish request created - requestId: {}, questionId: {}", savedRequest.getId(), questionId);

        return mapPublishRequestToDto(savedRequest, question);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<PublishRequestResponseDto> getPublishRequests(Pageable pageable) {
        log.info("Fetching all publish requests");

        Page<PublishRequest> requestsPage = publishRequestRepository
                .findByStatusAndIsDeletedFalse(PublishRequest.PublishRequestStatus.PENDING, pageable);

        List<PublishRequestResponseDto> dtos = requestsPage.getContent().stream().map(request -> {
            QuestionBankItem question = questionRepository.findById(request.getQuestionId()).orElse(null);
            return mapPublishRequestToDto(request, question);
        }).collect(Collectors.toList());

        PaginationDto paginationInfo = PaginationDto.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(requestsPage.getSize())
                .totalPages(requestsPage.getTotalPages())
                .totalItems(requestsPage.getTotalElements())
                .build();

        return PaginatedResponseDto.<PublishRequestResponseDto>builder().data(dtos).pagination(paginationInfo).build();
    }

    @Transactional
    public PublishRequestResponseDto approvePublishRequest(String questionId) {
        log.info("Approving publish request - questionId: {}", questionId);

        QuestionBankItem question = questionRepository.findById(questionId).orElseThrow(() -> {
            log.warn("Question not found for approval - id: {}", questionId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found with id: " + questionId);
        });

        // Find the pending publish request for this question
        PublishRequest publishRequest = publishRequestRepository
                .findByQuestionIdAndIsDeletedFalse(questionId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No pending publish request found - questionId: {}", questionId);
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "No publish request found for this question");
                });

        // Update the request status to APPROVED
        publishRequest.setStatus(PublishRequest.PublishRequestStatus.APPROVED);
        publishRequestRepository.save(publishRequest);

        // Set the question's ownerId to null to make it public
        question.setOwnerId(null);
        questionRepository.save(question);

        log.info("Publish request approved - questionId: {}, requestId: {}", questionId, publishRequest.getId());

        return mapPublishRequestToDto(publishRequest, question);
    }

    private PublishRequestResponseDto mapPublishRequestToDto(PublishRequest request, QuestionBankItem question) {
        QuestionResponseDto questionDto = question != null ? questionMapper.toResponseDto(question) : null;

        return PublishRequestResponseDto.builder()
                .id(request.getId())
                .questionId(request.getQuestionId())
                .requesterId(request.getRequesterId())
                .status(request.getStatus().toString())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .question(questionDto)
                .build();
    }
}
