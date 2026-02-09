package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.service.DocumentService;

import com.datn.datnbe.document.entity.Assignment;
import com.datn.datnbe.document.dto.request.QuestionItemRequest;
import com.datn.datnbe.document.entity.Question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.datn.datnbe.document.mapper.AssignmentMapper;
import com.datn.datnbe.document.repository.AssignmentRepository;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentManagement implements AssignmentApi {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final SecurityContextUtils securityContextUtils;
    private final ResourcePermissionApi resourcePermissionApi;
    private final DocumentService documentVisitService;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        String userId = securityContextUtils.getCurrentUserId();
        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setOwnerId(userId);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            assignment.setQuestions(mapQuestionItems(request.getQuestions()));
        }

        Assignment saved = assignmentRepository.save(assignment);

        // Register resource
        ResourceRegistrationRequest resourceRequest = ResourceRegistrationRequest.builder()
                .id(saved.getId())
                .name(saved.getTitle())
                .resourceType("assignment")
                .build();
        resourcePermissionApi.registerResource(resourceRequest, userId);

        return assignmentMapper.toDto(saved);
    }

    @Override
    public PaginatedResponseDto<AssignmentResponse> getAssignments(int page, int size, String search) {
        String userId = securityContextUtils.getCurrentUserId();
        List<String> allowedIds = resourcePermissionApi.getAllResourceByTypeOfOwner(userId, "assignment");

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by("createdAt").descending());

        Page<Assignment> assignmentPage = assignmentRepository.findByIdIn(allowedIds, pageable);

        return PaginatedResponseDto.<AssignmentResponse>builder()
                .data(assignmentPage.getContent().stream().map(assignmentMapper::toDto).collect(Collectors.toList()))
                .pagination(PaginationDto.builder()
                        .currentPage(assignmentPage.getNumber() + 1)
                        .pageSize(assignmentPage.getSize())
                        .totalItems(assignmentPage.getTotalElements())
                        .totalPages(assignmentPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public AssignmentResponse getAssignmentById(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        String userId = securityContextUtils.getCurrentUserId();
        if (userId != null) {
            DocumentMetadataDto metadata = DocumentMetadataDto.builder()
                    .userId(userId)
                    .documentId(id)
                    .type("assignment")
                    .title(assignment.getTitle())
                    .thumbnail(null)
                    .build();
            documentVisitService.trackDocumentVisit(metadata);
        }

        return assignmentMapper.toDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        assignmentMapper.updateEntity(assignment, request);

        if (request.getQuestions() != null) {
            assignment.setQuestions(mapQuestionItems(request.getQuestions()));
        }

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    private List<Question> mapQuestionItems(List<QuestionItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(item -> Question.builder()
                        .id(item.getId())
                        .type(item.getType())
                        .difficulty(item.getDifficulty())
                        .title(item.getTitle())
                        .titleImageUrl(item.getTitleImageUrl())
                        .explanation(item.getExplanation())
                        .grade(item.getGrade())
                        .chapter(item.getChapter())
                        .subject(item.getSubject())
                        .contextId(item.getContextId())
                        .topicId(item.getTopicId())
                        .data(item.getData())
                        .point(item.getPoint())
                        .build())
                .collect(Collectors.toList());
    }
}
