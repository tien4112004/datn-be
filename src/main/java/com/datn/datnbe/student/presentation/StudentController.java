package com.datn.datnbe.student.presentation;

import com.datn.datnbe.student.api.StudentImportApi;
import com.datn.datnbe.student.dto.response.StudentImportResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
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
}
