package com.datn.datnbe.document.presentation;

import com.datn.datnbe.cms.entity.AssignmentPost;
import com.datn.datnbe.cms.repository.AssignmentPostRepository;
import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentSettingsUpdateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.mapper.AssignmentMapper;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import com.datn.datnbe.sharedkernel.security.annotation.RequireDocumentPermission;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentApi assignmentApi;
    private final AssignmentPostRepository assignmentPostRepository;
    private final AssignmentMapper assignmentMapper;

    @PostMapping
    public ResponseEntity<AppResponseDto<AssignmentResponse>> createAssignment(
            @RequestBody AssignmentCreateRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.createAssignment(request)));
    }

    @GetMapping
    public ResponseEntity<AppResponseDto<List<AssignmentResponse>>> getAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        PaginatedResponseDto<AssignmentResponse> paginated = assignmentApi.getAssignments(page, size, search);
        return ResponseEntity.ok(AppResponseDto.successWithPagination(paginated.getData(), paginated.getPagination()));
    }

    @GetMapping("/{id}")
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentById(@PathVariable String id) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.getAssignmentById(id)));
    }

    @PutMapping("/{id}")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<AssignmentResponse>> updateAssignment(@PathVariable String id,
            @RequestBody AssignmentUpdateRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.updateAssignment(id, request)));
    }

    @DeleteMapping("/{id}")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        assignmentApi.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // Assignment settings endpoints

    @GetMapping("/{id}/settings")
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentSettings(@PathVariable String id) {
        // Return full assignment which includes settings
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.getAssignmentById(id)));
    }

    @PutMapping("/{id}/settings")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<AssignmentResponse>> updateAssignmentSettings(@PathVariable String id,
            @RequestBody AssignmentSettingsUpdateRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.updateAssignmentSettings(id, request)));
    }

    /**
     * Get assignment by ID without document permission check.
     * This endpoint is for students accessing assignments through posts/submissions.
     * Fetches from assignment_post table (cloned assignments for posts).
     */
    @GetMapping("/{id}/public")
    public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentPublic(@PathVariable String id) {
        AssignmentPost assignmentPost = assignmentPostRepository.findAssignmentById(id);

        if (assignmentPost == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found");
        }

        AssignmentResponse response = assignmentMapper.toDto(assignmentPost);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
