package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionApi submissionApi;

    @PostMapping("/lessons/{lessonId}/submissions")
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> createSubmission(@PathVariable String lessonId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file) {
        log.debug("POST /api/lessons/{}/submissions", lessonId);
        SubmissionResponseDto response = submissionApi.createSubmission(lessonId, content, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/lessons/{lessonId}/submissions")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<List<SubmissionResponseDto>>> getSubmissions(@PathVariable String lessonId) {
        log.debug("GET /api/lessons/{}/submissions", lessonId);
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissions(lessonId)));
    }


    @PostMapping("/posts/{postId}/submissions")
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> createSubmission(@PathVariable String postId,
            @RequestParam String studentId) {
        SubmissionResponseDto response = submissionApi.createSubmission(postId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/posts/{postId}/submissions")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<List<SubmissionResponseDto>>> getSubmissionsV2(@PathVariable String postId) {
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissionsV2(postId)));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<AppResponseDto<SubmissionResponseDto>> getSubmissionById(@PathVariable String id) {
        log.debug("GET /api/submissions/{}", id);
        return ResponseEntity.ok(AppResponseDto.success(submissionApi.getSubmissionById(id)));
    }

    @DeleteMapping("/submissions/{id}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deleteSubmission(@PathVariable String id) {
        log.debug("DELETE /api/submissions/{}", id);
        submissionApi.deleteSubmission(id);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
