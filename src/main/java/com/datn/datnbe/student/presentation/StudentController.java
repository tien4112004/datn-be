package com.datn.datnbe.student.presentation;

import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentEnrollmentRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentController {

    StudentImportApi studentImportApi;
    StudentApi studentApi;

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<AppResponseDto<List<StudentResponseDto>>> getStudentsByClass(@PathVariable String classId) {
        log.info("Received request to get students for class: {}", classId);
        List<StudentResponseDto> students = studentApi.getStudentsByClass(classId);
        return ResponseEntity.ok(AppResponseDto.success(students, "Students retrieved successfully"));
    }

    @PostMapping("/classes/{classId}/students")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> enrollStudent(@PathVariable String classId,
            @RequestBody String jsonRequest) throws Exception {
        log.info("Received request to enroll student in class: {}", classId);

        // Use a simple JSON parsing to determine the type
        StudentResponseDto response;

        if (jsonRequest.contains("\"studentId\"") && !jsonRequest.contains("\"firstName\"")) {
            // It's an enrollment request
            StudentEnrollmentRequest enrollmentRequest = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(jsonRequest, StudentEnrollmentRequest.class);
            response = studentApi.enrollStudent(classId, enrollmentRequest.getStudentId());
        } else {
            // It's a create request
            StudentCreateRequest createRequest = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(jsonRequest, StudentCreateRequest.class);
            response = studentApi.createAndEnrollStudent(classId, createRequest);
        }

        return ResponseEntity.ok(AppResponseDto.success(response, "Student enrolled successfully"));
    }

    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<AppResponseDto<Void>> removeStudentFromClass(@PathVariable String classId,
            @PathVariable String studentId) {
        log.info("Received request to remove student {} from class {}", studentId, classId);
        studentApi.removeStudentFromClass(classId, studentId);
        return ResponseEntity.noContent().<AppResponseDto<Void>>build();
    }

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<AppResponseDto<?>> getStudentsByClass(@PathVariable String classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received request to get students for class: {} with pagination", classId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var response = studentApi.getStudentsByClass(classId, pageable);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    @PostMapping("/classes/{classId}/students")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> createAndEnrollStudent(@PathVariable String classId,
            @Valid @RequestBody StudentCreateRequest createRequest) {
        log.info("Received request to create and enroll student in class: {}", classId);

        StudentResponseDto createdStudent = studentApi.createStudent(createRequest);
        studentApi.enrollStudent(classId, createdStudent.getId());
        return ResponseEntity.ok(AppResponseDto.success(createdStudent));
    }

    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<AppResponseDto<Void>> removeStudentFromClass(@PathVariable String classId,
            @PathVariable String studentId) {
        log.info("Received request to remove student {} from class {}", studentId, classId);
        studentApi.removeStudentFromClass(classId, studentId);
        return ResponseEntity.noContent().<AppResponseDto<Void>>build();
    }

    @PostMapping("/classes/{classId}/students/import")
    public ResponseEntity<AppResponseDto<StudentImportResponseDto>> importStudents(@PathVariable String classId,
            @RequestParam(value = "file", required = true) MultipartFile file) {
        log.info("Received student import request for class {} with file: {}",
                classId,
                file != null ? file.getOriginalFilename() : "null");

        StudentImportResponseDto result = studentImportApi.importStudentsFromCsv(file);

        if (result.isSuccess()) {
            return ResponseEntity.ok(AppResponseDto.success(result, result.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(AppResponseDto.<StudentImportResponseDto>builder()
                            .success(false)
                            .code(400)
                            .data(result)
                            .message(result.getMessage())
                            .build());
        }
    }

    @PutMapping("/students/{studentId}")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> updateStudent(@PathVariable String studentId,
            @Valid @RequestBody StudentUpdateRequest request) {
        log.info("Received request to update student with ID: {}", studentId);
        StudentResponseDto response = studentApi.updateStudent(studentId, request);
        return ResponseEntity.ok(AppResponseDto.success(response, "Student updated successfully"));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> getStudentById(@PathVariable String studentId) {
        log.info("Received request to get student with ID: {}", studentId);
        StudentResponseDto response = studentApi.getStudentById(studentId);
        return ResponseEntity.ok(AppResponseDto.success(response, "Student retrieved successfully"));
    }
}
