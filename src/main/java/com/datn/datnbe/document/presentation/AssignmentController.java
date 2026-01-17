package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.request.AddQuestionRequest;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.response.AssignmentQuestionInfo;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

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

    @PostMapping("/{id}/questions")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<AppResponseDto<AssignmentQuestionInfo>> addQuestionToAssignment(@PathVariable String id,
            @RequestBody AddQuestionRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.addQuestionToAssignment(id, request)));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @RequireDocumentPermission(scopes = {"edit"})
    public ResponseEntity<Void> removeQuestionFromAssignment(@PathVariable String id, @PathVariable String questionId) {
        assignmentApi.removeQuestionFromAssignment(id, questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/questions")
    @RequireDocumentPermission(scopes = {"read"})
    public ResponseEntity<AppResponseDto<List<AssignmentQuestionInfo>>> getAssignmentQuestions(
            @PathVariable String id) {
        return ResponseEntity.ok(AppResponseDto.success(assignmentApi.getAssignmentQuestions(id)));
    }
}
