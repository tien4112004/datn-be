package com.datn.datnbe.document.exam.presentation;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.document.exam.api.ExamApi;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.exam.dto.response.ExamDraftDto;
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
     * Generate an exam matrix using AI.
     */
    @Operation(summary = "Generate Exam Matrix", description = """
            Generate an exam matrix using AI. The matrix has 3 dimensions:
            - **Topics** (first dimension): List of subject topics
            - **Difficulties** (second dimension): KNOWLEDGE, COMPREHENSION, APPLICATION
            - **Question Types** (third dimension): MULTIPLE_CHOICE, FILL_IN_BLANK, TRUE_FALSE, MATCHING

            Each cell is a string `"count:points"` representing:
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
                          "difficulties": ["KNOWLEDGE", "COMPREHENSION", "APPLICATION"],
                          "questionTypes": ["MULTIPLE_CHOICE", "TRUE_FALSE"]
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
    @PostMapping("/generate-matrix")
    public ResponseEntity<AppResponseDto<ExamMatrixDto>> generateMatrix(
            @Valid @RequestBody GenerateMatrixRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID teacherId = extractTeacherId(jwt);
        log.info("Generate matrix request from teacher: {} for topics: {}", teacherId, request.getTopics());

        ExamMatrixDto matrix = examApi.generateMatrix(request, teacherId);
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
        UUID teacherId = extractTeacherId(jwt);
        log.info("Generate exam from matrix request from teacher: {}", teacherId);

        ExamDraftDto draft = examApi.generateExamFromMatrix(request, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(draft));
    }

    /**
     * Extract and validate teacher ID from JWT token.
     *
     * @param jwt the JWT token, may be null
     * @return the teacher UUID, or null if jwt is null or subject is not a valid UUID
     */
    private UUID extractTeacherId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return null;
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format in JWT subject: {}", jwt.getSubject());
            return null;
        }
    }
}
