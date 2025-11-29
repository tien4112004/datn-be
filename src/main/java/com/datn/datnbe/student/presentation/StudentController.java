package com.datn.datnbe.student.presentation;

import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Import students from a CSV file.
     *
     * @param file the CSV file containing student data
     * @return response with import results
     */
    @PostMapping("/import")
    public ResponseEntity<AppResponseDto<StudentImportResponseDto>> importStudents(
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

    /**
     * Get a student by ID.
     *
     * @param id the student ID
     * @return the student data
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> getStudent(@PathVariable String id) {
        log.info("Received request to get student with ID: {}", id);
        StudentResponseDto response = studentApi.getStudentById(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Update a student by ID.
     *
     * @param id      the student ID
     * @param request the update request
     * @return the updated student data
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<StudentResponseDto>> updateStudent(@PathVariable String id,
            @Valid @RequestBody StudentUpdateRequest request) {

        log.info("Received request to update student with ID: {}", id);
        StudentResponseDto response = studentApi.updateStudent(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response, "Student updated successfully"));
    }

    /**
     * Delete a student by ID.
     *
     * @param id the student ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> deleteStudent(@PathVariable String id) {
        log.info("Received request to delete student with ID: {}", id);
        studentApi.deleteStudent(id);
        return ResponseEntity.ok(AppResponseDto.success("Student deleted successfully"));
    }
}
