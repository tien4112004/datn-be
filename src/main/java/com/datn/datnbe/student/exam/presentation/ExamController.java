package com.datn.datnbe.student.exam.presentation;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

    ExamApi examApi;

    @PostMapping("/generate-matrix")
    public ResponseEntity<AppResponseDto<ExamMatrixDto>> generateMatrix(
            @Valid @RequestBody GenerateMatrixRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        log.info("Generate matrix request from teacher: {}", teacherId);

        ExamMatrixDto matrix = examApi.generateMatrix(request);
        return ResponseEntity.ok(AppResponseDto.success(matrix));
    }
}
