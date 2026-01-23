package com.datn.datnbe.student.exam.presentation;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.ExamMatrixV2Dto;
import com.datn.datnbe.student.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixV2Request;
import com.datn.datnbe.student.exam.dto.response.ExamDraftDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Exam Generation", description = "APIs for generating exam matrices and exams from question bank")
public class ExamController {

    ExamApi examApi;

    /**
     * Generate an exam matrix using AI (legacy format).
     */
    @Operation(summary = "Generate Exam Matrix (Legacy)", description = "Generate an exam matrix using AI in legacy format. Use V2 endpoint for new integrations.")
    @PostMapping("/generate-matrix")
    public ResponseEntity<AppResponseDto<ExamMatrixDto>> generateMatrix(
            @Valid @RequestBody GenerateMatrixRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        log.info("Generate matrix request from teacher: {}", teacherId);

        ExamMatrixDto matrix = examApi.generateMatrix(request);
        return ResponseEntity.ok(AppResponseDto.success(matrix));
    }

    /**
     * Generate a 3D exam matrix using AI.
     */
    @Operation(summary = "Generate 3D Exam Matrix (V2)", description = """
            Generate a 3D exam matrix using AI. The matrix has dimensions:
            - **Topics** (first dimension): List of subject topics
            - **Difficulties** (second dimension): easy, medium, hard
            - **Question Types** (third dimension): multiple_choice, fill_in_blank, true_false, matching

            Each cell is an array `[count, points]` representing:
            - `count`: Number of questions for that combination
            - `points`: Total points allocated for that combination
            """, responses = {
            @ApiResponse(responseCode = "200", description = "Matrix generated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "success": true,
                      "code": 200,
                      "data": {
                        "metadata": {
                          "id": "uuid-here",
                          "name": "Math Exam Matrix",
                          "createdAt": "2026-01-22T10:00:00Z"
                        },
                        "dimensions": {
                          "topics": [
                            {"id": "topic-1", "name": "Algebra"},
                            {"id": "topic-2", "name": "Geometry"}
                          ],
                          "difficulties": ["easy", "medium", "hard"],
                          "questionTypes": ["multiple_choice", "true_false"]
                        },
                        "matrix": [
                          [
                            [[3, 15], [2, 10]],
                            [[2, 20], [1, 10]],
                            [[1, 15], [1, 10]]
                          ],
                          [
                            [[2, 10], [2, 10]],
                            [[2, 20], [0, 0]],
                            [[1, 15], [0, 0]]
                          ]
                        ],
                        "totalQuestions": 17,
                        "totalPoints": 135.0
                      }
                    }
                    """)))})
    @PostMapping("/generate-matrix/v2")
    public ResponseEntity<AppResponseDto<ExamMatrixV2Dto>> generateMatrixV2(
            @Valid @RequestBody GenerateMatrixV2Request request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        log.info("Generate V2 matrix request from teacher: {} for topics: {}", teacherId, request.getTopics());

        ExamMatrixV2Dto matrix = examApi.generateMatrixV2(request);
        return ResponseEntity.ok(AppResponseDto.success(matrix));
    }

    /**
     * Generate an exam by selecting questions from the question bank.
     */
    @Operation(summary = "Generate Exam from Matrix", description = """
            Generate an exam by selecting questions from the question bank based on the provided matrix.

            This endpoint:
            1. Takes a matrix defining question requirements
            2. Queries the question bank for matching questions
            3. Returns a draft exam with selected questions (and any gaps)

            **Missing Question Strategies:**
            - `REPORT_GAPS`: Return draft with gaps indicated (default)
            - `GENERATE_WITH_AI`: Use AI to generate missing questions
            - `FAIL_FAST`: Reject if not all requirements can be met
            """)
    @ApiResponse(responseCode = "200", description = "Exam draft generated successfully")
    @PostMapping("/generate-from-matrix")
    public ResponseEntity<AppResponseDto<ExamDraftDto>> generateExamFromMatrix(
            @Valid @RequestBody GenerateExamFromMatrixRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        log.info("Generate exam from matrix request from teacher: {}", teacherId);

        ExamDraftDto draft = examApi.generateExamFromMatrix(request, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(draft));
    }
}
