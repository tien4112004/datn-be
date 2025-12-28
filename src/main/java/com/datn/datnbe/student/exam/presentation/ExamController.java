package com.datn.datnbe.student.exam.presentation;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.dto.request.CreateExamRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateQuestionsRequest;
import com.datn.datnbe.student.exam.dto.request.UpdateExamRequest;
import com.datn.datnbe.student.exam.dto.response.ExamDetailDto;
import com.datn.datnbe.student.exam.dto.response.ExamResponseDto;
import com.datn.datnbe.student.exam.dto.response.ExamSummaryDto;
import com.datn.datnbe.student.exam.enums.ExamStatus;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

    ExamApi examApi;

    @PostMapping
    public ResponseEntity<AppResponseDto<ExamResponseDto>> createExam(@Valid @RequestBody CreateExamRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Create exam request from teacher: {}", teacherId);

        ExamResponseDto response = examApi.createExam(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping
    public ResponseEntity<AppResponseDto<PaginatedResponseDto<ExamSummaryDto>>> getAllExams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) ExamStatus status,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) GradeLevel gradeLevel,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Get all exams request from teacher: {}", teacherId);

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        PaginatedResponseDto<ExamSummaryDto> response = examApi
                .getAllExams(teacherId, pageable, status, topic, gradeLevel);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<ExamDetailDto>> getExamById(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Get exam {} request from teacher: {}", id, teacherId);

        ExamDetailDto response = examApi.getExamById(id, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<ExamDetailDto>> updateExam(@PathVariable UUID id,
            @Valid @RequestBody UpdateExamRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Update exam {} request from teacher: {}", id, teacherId);

        ExamDetailDto response = examApi.updateExam(id, request, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> deleteExam(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Delete exam {} request from teacher: {}", id, teacherId);

        examApi.deleteExam(id, teacherId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AppResponseDto<Void>> archiveExam(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Archive exam {} request from teacher: {}", id, teacherId);

        examApi.archiveExam(id, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(null));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<AppResponseDto<ExamResponseDto>> duplicateExam(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Duplicate exam {} request from teacher: {}", id, teacherId);

        ExamResponseDto response = examApi.duplicateExam(id, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PostMapping(value = "/generate-matrix", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateMatrix(@Valid @RequestBody GenerateMatrixRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Generate matrix request from teacher: {}", teacherId);

        return examApi.generateMatrix(request);
    }

    @PostMapping(value = "/{id}/generate-questions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateQuestions(@PathVariable UUID id,
            @Valid @RequestBody GenerateQuestionsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = UUID.fromString(jwt.getSubject());
        log.info("Generate questions for exam {} request from teacher: {}", id, teacherId);

        return examApi.generateQuestions(id, request, teacherId);
    }
}
