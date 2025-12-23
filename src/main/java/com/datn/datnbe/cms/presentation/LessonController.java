package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.LessonApi;
import com.datn.datnbe.cms.dto.request.LessonCreateRequest;
import com.datn.datnbe.cms.dto.request.LessonUpdateRequest;
import com.datn.datnbe.cms.dto.response.LessonResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LessonController {

    private final LessonApi lessonApi;

    @PostMapping({"/classes/{classId}/lessons"})
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<LessonResponseDto>> createClassLesson(@PathVariable String classId,
            @Valid @RequestBody LessonCreateRequest request) {
        log.debug("POST /api/classes/{}/lessons", classId);
        LessonResponseDto response = lessonApi.createLesson(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping({"/classes/{classId}/lessons"})
    public ResponseEntity<AppResponseDto<List<LessonResponseDto>>> getClassLessons(@PathVariable String classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.debug("GET /api/classes/{}/lessons", classId);
        PaginatedResponseDto<LessonResponseDto> paginated = lessonApi
                .getLessons(classId, Math.max(0, page - 1), size, search);
        return ResponseEntity.ok(AppResponseDto.successWithPagination(paginated.getData(), paginated.getPagination()));
    }

    @PostMapping({"/lessons"})
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<LessonResponseDto>> createGlobalLesson(
            @Valid @RequestBody LessonCreateRequest request) {
        log.debug("POST /api/lessons");
        LessonResponseDto response = lessonApi.createLesson(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping({"/lessons"})
    public ResponseEntity<AppResponseDto<List<LessonResponseDto>>> getAllLessons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.debug("GET /api/lessons");
        PaginatedResponseDto<LessonResponseDto> paginated = lessonApi
                .getLessons(null, Math.max(0, page - 1), size, search);
        return ResponseEntity.ok(AppResponseDto.successWithPagination(paginated.getData(), paginated.getPagination()));
    }

    @GetMapping({"/lessons/{id}"})
    public ResponseEntity<AppResponseDto<LessonResponseDto>> getLessonById(@PathVariable String id) {
        log.debug("GET /api/lessons/{}", id);
        LessonResponseDto response = lessonApi.getLessonById(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping({"/lessons/{id}"})
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<LessonResponseDto>> updateLesson(@PathVariable String id,
            @Valid @RequestBody LessonUpdateRequest request) {
        log.debug("PUT /api/lessons/{}", id);
        LessonResponseDto response = lessonApi.updateLesson(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping({"/lessons/{id}"})
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deleteLesson(@PathVariable String id) {
        log.debug("DELETE /api/lessons/{}", id);
        lessonApi.deleteLesson(id);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
