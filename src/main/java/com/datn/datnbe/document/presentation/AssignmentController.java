package com.datn.datnbe.document.presentation;

import com.datn.datnbe.cms.entity.AssignmentPost;
import com.datn.datnbe.cms.repository.AssignmentPostRepository;
import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentSettingsUpdateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.request.GenerateAssignmentFromMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.mapper.AssignmentMapper;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import com.datn.datnbe.document.dto.response.AssignmentDraftDto;

import com.datn.datnbe.sharedkernel.security.annotation.RequireDocumentPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "APIs for assignment and exam management")
public class AssignmentController {

  private final AssignmentApi assignmentApi;
  private final AssignmentPostRepository assignmentPostRepository;
  private final AssignmentMapper assignmentMapper;
  private final SecurityContextUtils securityContextUtils;

  @PostMapping
  public ResponseEntity<AppResponseDto<AssignmentResponse>> createAssignment(
      @RequestBody AssignmentCreateRequest request) {
    return ResponseEntity.ok(AppResponseDto.success(assignmentApi.createAssignment(request)));
  }

  @GetMapping
  public ResponseEntity<AppResponseDto<List<AssignmentResponse>>> getAssignments(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search) {

    PaginatedResponseDto<AssignmentResponse> paginated = assignmentApi.getAssignments(page, size, search);
    return ResponseEntity.ok(AppResponseDto.successWithPagination(paginated.getData(), paginated.getPagination()));
  }

  @GetMapping("/{id}")
  @RequireDocumentPermission(scopes = { "read" })
  public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentById(@PathVariable String id) {
    return ResponseEntity.ok(AppResponseDto.success(assignmentApi.getAssignmentById(id)));
  }

  @PutMapping("/{id}")
  @RequireDocumentPermission(scopes = { "edit" })
  public ResponseEntity<AppResponseDto<AssignmentResponse>> updateAssignment(@PathVariable String id,
      @RequestBody AssignmentUpdateRequest request) {
    return ResponseEntity.ok(AppResponseDto.success(assignmentApi.updateAssignment(id, request)));
  }

  @DeleteMapping("/{id}")
  @RequireDocumentPermission(scopes = { "edit" })
  public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
    assignmentApi.deleteAssignment(id);
    return ResponseEntity.noContent().build();
  }

  // Assignment settings endpoints

  @GetMapping("/{id}/settings")
  @RequireDocumentPermission(scopes = { "read" })
  public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentSettings(@PathVariable String id) {
    // Return full assignment which includes settings
    return ResponseEntity.ok(AppResponseDto.success(assignmentApi.getAssignmentById(id)));
  }

  @PutMapping("/{id}/settings")
  @RequireDocumentPermission(scopes = { "edit" })
  public ResponseEntity<AppResponseDto<AssignmentResponse>> updateAssignmentSettings(@PathVariable String id,
      @RequestBody AssignmentSettingsUpdateRequest request) {
    return ResponseEntity.ok(AppResponseDto.success(assignmentApi.updateAssignmentSettings(id, request)));
  }

  /**
   * Get assignment by ID without document permission check.
   * This endpoint is for students accessing assignments through posts/submissions.
   * Fetches from assignment_post table (cloned assignments for posts).
   */
  @GetMapping("/{id}/public")
  public ResponseEntity<AppResponseDto<AssignmentResponse>> getAssignmentPublic(@PathVariable String id) {
    AssignmentPost assignmentPost = assignmentPostRepository.findAssignmentById(id);

    if (assignmentPost == null) {
      throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found");
    }

    AssignmentResponse response = assignmentMapper.toDto(assignmentPost);
    return ResponseEntity.ok(AppResponseDto.success(response));
  }

  /**
   * Generate an exam matrix using AI.
   */
  @Operation(summary = "Generate Exam Matrix", description = """
      Generate an exam matrix using AI. The matrix has 3 dimensions:
      - **Topics** (first dimension): List of subject topics
      - **Difficulties** (second dimension): KNOWLEDGE, COMPREHENSION, APPLICATION
      - **Question Types** (third dimension): MULTIPLE_CHOICE, FILL_IN_BLANK, OPEN_ENDED, MATCHING

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
                "questionTypes": ["MULTIPLE_CHOICE", "OPEN_ENDED"]
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
          """))) })
  @PostMapping("/generate-matrix")
  public ResponseEntity<AppResponseDto<AssignmentMatrixDto>> generateMatrix(
      @Valid @RequestBody GenerateMatrixRequest request) {
    String teacherId = securityContextUtils.getCurrentUserId();
    log.info("Generate matrix request from teacher: {} for grade: {}, subject: {}",
        teacherId,
        request.getGrade(),
        request.getSubject());

    AssignmentMatrixDto matrix = assignmentApi.generateMatrix(request, teacherId);
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
  public ResponseEntity<AppResponseDto<AssignmentDraftDto>> generateAssignmentFromMatrix(
      @Valid @RequestBody GenerateAssignmentFromMatrixRequest request) {
    String teacherId = securityContextUtils.getCurrentUserId();
    log.info("Generate exam from matrix request from teacher: {}", teacherId);

    AssignmentDraftDto draft = assignmentApi.generateAssignmentFromMatrix(request, teacherId);
    return ResponseEntity.ok(AppResponseDto.success(draft));
  }
}
