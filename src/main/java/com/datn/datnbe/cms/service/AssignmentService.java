package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.cms.api.AssignmentApi;
import com.datn.datnbe.cms.dto.request.AddQuestionRequest;
import com.datn.datnbe.cms.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.cms.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.cms.dto.response.AssignmentQuestionInfo;
import com.datn.datnbe.cms.dto.response.AssignmentResponse;
import com.datn.datnbe.cms.entity.Assignment;
import com.datn.datnbe.cms.entity.AssignmentQuestion;
import com.datn.datnbe.cms.entity.QuestionBankItem;
import com.datn.datnbe.cms.mapper.AssignmentMapper;
import com.datn.datnbe.cms.mapper.QuestionEntityMapper;
import com.datn.datnbe.cms.repository.AssignmentQuestionRepository;
import com.datn.datnbe.cms.repository.AssignmentRepository;
import com.datn.datnbe.cms.repository.QuestionRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AssignmentService implements AssignmentApi {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentQuestionRepository assignmentQuestionRepository;
    private final QuestionRepository questionRepository;
    private final AssignmentMapper assignmentMapper;
    private final QuestionEntityMapper questionMapper;
    private final SecurityContextUtils securityContextUtils;
    private final ResourcePermissionApi resourcePermissionApi;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        String userId = securityContextUtils.getCurrentUserId();
        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setOwnerId(userId);

        Assignment saved = assignmentRepository.save(assignment);

        // Register resource
        ResourceRegistrationRequest resourceRequest = ResourceRegistrationRequest
                .builder()
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
        return assignmentMapper.toDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        // Permission check handled by Aspect

        assignmentMapper.updateEntity(assignment, request);
        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAssignment(String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        // Permission check handled by Aspect

        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional
    public AssignmentQuestionInfo addQuestionToAssignment(String assignmentId, AddQuestionRequest request) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        QuestionBankItem question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found"));

        // Check if already exists
        if (assignmentQuestionRepository.findByAssignmentIdAndQuestionId(assignmentId, request.getQuestionId())
                .isPresent()) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Question already exists in this assignment");
        }

        AssignmentQuestion link = AssignmentQuestion.builder()
                .assignmentId(assignmentId)
                .questionId(request.getQuestionId())
                .point(request.getPoint())
                .order(request.getOrder())
                .build();

        AssignmentQuestion savedLink = assignmentQuestionRepository.save(link);

        return AssignmentQuestionInfo.builder()
                .id(savedLink.getId())
                .assignmentId(assignmentId)
                .question(questionMapper.toResponseDto(question))
                .point(savedLink.getPoint())
                .order(savedLink.getOrder())
                .build();
    }

    @Override
    @Transactional
    public void removeQuestionFromAssignment(String assignmentId, String questionId) {
        AssignmentQuestion link = assignmentQuestionRepository.findByAssignmentIdAndQuestionId(assignmentId, questionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found in assignment"));

        assignmentQuestionRepository.delete(link);
    }

    @Override
    public List<AssignmentQuestionInfo> getAssignmentQuestions(String assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found");
        }

        List<AssignmentQuestion> links = assignmentQuestionRepository.findByAssignmentIdOrderByOrderAsc(assignmentId);

        return links.stream().map(link -> {
            QuestionBankItem question = questionRepository.findById(link.getQuestionId()).orElse(null);
            // If question deleted, might handle gracefully or skip
            if (question == null)
                return null;

            return AssignmentQuestionInfo.builder()
                    .id(link.getId())
                    .assignmentId(assignmentId)
                    .question(questionMapper.toResponseDto(question))
                    .point(link.getPoint())
                    .order(link.getOrder())
                    .build();
        }).filter(item -> item != null).collect(Collectors.toList());
    }
}
