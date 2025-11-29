package com.datn.datnbe.student.presentation;

import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
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

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentController {

    StudentImportApi studentImportApi;
    StudentApi studentApi;

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

        log.info("Received student import request for file: {}", file != null ? file.getOriginalFilename() : "null");

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
}
