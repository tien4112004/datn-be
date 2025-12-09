package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.ClassApi;
import com.datn.datnbe.cms.api.SeatingLayoutApi;
import com.datn.datnbe.cms.dto.request.ClassCollectionRequest;
import com.datn.datnbe.cms.dto.request.ClassCreateRequest;
import com.datn.datnbe.cms.dto.request.ClassUpdateRequest;
import com.datn.datnbe.cms.dto.request.SeatingLayoutRequest;
import com.datn.datnbe.cms.dto.response.ClassListResponseDto;
import com.datn.datnbe.cms.dto.response.ClassResponseDto;
import com.datn.datnbe.cms.dto.response.SeatingLayoutResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;

import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassController {

    private final ClassApi classApi;
    private final SeatingLayoutApi seatingLayoutApi;

    private final SecurityContextUtils securityContextUtils;

    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<ClassListResponseDto>>> getClasses(
            @Valid @ModelAttribute ClassCollectionRequest request) {
        log.debug("GET /api/classes with params: {}", request);

        var teacherId = securityContextUtils.getCurrentUserId();
        PaginatedResponseDto<ClassListResponseDto> paginatedResponse = classApi.getAllClasses(request, teacherId);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<ClassResponseDto>> createClass(
            @Valid @RequestBody ClassCreateRequest request) {
        log.debug("POST /api/classes with body: {}", request);

        ClassResponseDto response = classApi.createClass(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppResponseDto.<ClassResponseDto>builder()
                        .success(true)
                        .code(HttpStatus.CREATED.value())
                        .data(response)
                        .build());
    }

    @GetMapping("/{classId}")
    public ResponseEntity<AppResponseDto<ClassResponseDto>> getClassById(@PathVariable String classId) {
        log.debug("GET /api/classes/{}", classId);

        ClassResponseDto response = classApi.getClassById(classId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{classId}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<ClassResponseDto>> updateClass(@PathVariable String classId,
            @Valid @RequestBody ClassUpdateRequest request) {
        log.debug("PUT /api/classes/{} with body: {}", classId, request);

        ClassResponseDto response = classApi.updateClass(classId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    // ============== Seating Chart Endpoints ==============

    @GetMapping("/{classId}/seating-chart")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<SeatingLayoutResponseDto>> getSeatingChart(@PathVariable String classId) {
        log.debug("GET /api/classes/{}/seating-chart", classId);

        SeatingLayoutResponseDto response = seatingLayoutApi.getSeatingChart(classId);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{classId}/seating-chart")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<SeatingLayoutResponseDto>> saveSeatingChart(@PathVariable String classId,
            @Valid @RequestBody SeatingLayoutRequest request) {
        log.debug("PUT /api/classes/{}/seating-chart with body: {}", classId, request);

        SeatingLayoutResponseDto response = seatingLayoutApi.saveSeatingChart(classId, request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
