package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.request.SubmissionGradeRequest;
import com.datn.datnbe.cms.dto.request.SubmissionValidationRequest;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionStatisticsDto;
import com.datn.datnbe.cms.dto.response.SubmissionValidationResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionApi submissionApi;

    @PostMapping("/posts/{postId}/submissions")
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> createSubmission(@PathVariable String postId,
            @RequestBody SubmissionCreateRequest request) {
        SubmissionResponseDto response = submissionApi.createSubmission(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/posts/{postId}/submissions")
    public ResponseEntity<AppResponseDto<List<SubmissionResponseDto>>> getSubmissions(@PathVariable String postId) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissions(postId)));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> getSubmissionById(@PathVariable String id) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissionById(id)));
    }

    @DeleteMapping("/submissions/{id}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deleteSubmission(@PathVariable String id) {
        log.debug("DELETE /api/submissions/{}", id);
        submissionApi.deleteSubmission(id);
        return ResponseEntity.ok(AppResponseDto.success());
    }

    @PutMapping("/submissions/{id}/grade")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> gradeSubmissionManually(@PathVariable String id,
            @RequestBody SubmissionGradeRequest request) {
        SubmissionResponseDto response = submissionApi.gradeSubmissionManually(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    // New endpoints for assignment feature alignment

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<AppResponseDto<List<SubmissionResponseDto>>> getAssignmentSubmissions(
            @PathVariable String assignmentId,
            @RequestParam(required = false) String studentId) {
        List<SubmissionResponseDto> submissions;
        if (studentId != null) {
            submissions = submissionApi.getSubmissionsByAssignmentIdAndStudentId(assignmentId, studentId);
        } else {
            submissions = submissionApi.getSubmissionsByAssignmentId(assignmentId);
        }
        return ResponseEntity.ok(AppResponseDto.success(submissions));
    }

    @GetMapping("/posts/{postId}/submissions/statistics")
    public ResponseEntity<AppResponseDto<SubmissionStatisticsDto>> getPostStatistics(@PathVariable String postId) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissionStatistics(postId)));
    }

    @GetMapping("/assignments/{assignmentId}/submissions/statistics")
    public ResponseEntity<AppResponseDto<SubmissionStatisticsDto>> getAssignmentStatistics(
            @PathVariable String assignmentId) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getAssignmentStatistics(assignmentId)));
    }

    @PostMapping("/assignments/{assignmentId}/validate-submission")
    public ResponseEntity<AppResponseDto<SubmissionValidationResponse>> validateSubmission(
            @PathVariable String assignmentId,
            @RequestBody SubmissionValidationRequest request) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.validateSubmission(assignmentId, request)));
    }
}
