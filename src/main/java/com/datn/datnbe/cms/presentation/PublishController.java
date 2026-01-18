package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.document.api.QuestionApi;
import com.datn.datnbe.document.dto.response.PublishRequestResponseDto;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publish")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PublishController {

    QuestionApi questionApi;
    SecurityContextUtils securityContextUtils;

    /**
     * GET /api/publish - User gets all published questions
     */
    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<QuestionResponseDto>>> getPublishedQuestions(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        log.info("Fetching published questions - page: {}, pageSize: {}", page, pageSize);

        if (page < 1) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page number must be >= 1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        PaginatedResponseDto<QuestionResponseDto> paginatedResponse = questionApi.getPublishedQuestions(pageable);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    /**
     * POST /api/publish/{questionId} - User publishes a question
     */
    @PostMapping("/{questionId}")
    public ResponseEntity<AppResponseDto<?>> publishQuestion(@PathVariable String questionId) {

        String currentUserId = securityContextUtils.getCurrentUserId();
        log.info("Processing publish request - questionId: {}, userId: {}", questionId, currentUserId);

        PublishRequestResponseDto response = questionApi.publishQuestion(questionId, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    /**
     * GET /api/admin/publish - Admin gets all publish requests
     */
    @GetMapping("/admin/publish")
    public ResponseEntity<AppResponseDto<List<PublishRequestResponseDto>>> getPublishRequests(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        log.info("Admin fetching publish requests - page: {}, pageSize: {}", page, pageSize);

        if (page < 1) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page number must be >= 1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        PaginatedResponseDto<PublishRequestResponseDto> paginatedResponse = questionApi.getPublishRequests(pageable);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    /**
     * POST /api/admin/publish/{questionId} - Admin approves the publish request
     */
    @PostMapping("/admin/publish/{questionId}")
    public ResponseEntity<AppResponseDto<?>> approvePublishRequest(@PathVariable String questionId) {

        log.info("Admin approving publish request - questionId: {}", questionId);

        PublishRequestResponseDto response = questionApi.approvePublishRequest(questionId);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
