package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.LessonResourceApi;
import com.datn.datnbe.cms.dto.request.LessonResourceCreateRequest;
import com.datn.datnbe.cms.dto.response.LessonResourceResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
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
public class LessonResourceController {

    private final LessonResourceApi lessonResourceApi;

    @PostMapping("/lessons/{lessonId}/resources")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<LessonResourceResponseDto>> createResource(@PathVariable String lessonId,
            @Valid @RequestBody LessonResourceCreateRequest request) {
        log.debug("POST /api/lessons/{}/resources", lessonId);
        LessonResourceResponseDto response = lessonResourceApi.createResource(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/lessons/{lessonId}/resources")
    public ResponseEntity<AppResponseDto<List<LessonResourceResponseDto>>> getResources(@PathVariable String lessonId) {
        log.debug("GET /api/lessons/{}/resources", lessonId);
        return ResponseEntity.ok(AppResponseDto.success(lessonResourceApi.getResources(lessonId)));
    }

    @GetMapping("/lesson-resources/{id}")
    public ResponseEntity<AppResponseDto<LessonResourceResponseDto>> getResourceById(@PathVariable String id) {
        log.debug("GET /api/lesson-resources/{}", id);
        return ResponseEntity.ok(AppResponseDto.success(lessonResourceApi.getResourceById(id)));
    }

    @PutMapping("/lesson-resources/{id}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<LessonResourceResponseDto>> updateResource(@PathVariable String id,
            @Valid @RequestBody LessonResourceCreateRequest request) {
        log.debug("PUT /api/lesson-resources/{}", id);
        return ResponseEntity.ok(AppResponseDto.success(lessonResourceApi.updateResource(id, request)));
    }

    @DeleteMapping("/lesson-resources/{id}")
    @RequireTeacherPermission
    public ResponseEntity<AppResponseDto<Void>> deleteResource(@PathVariable String id) {
        log.debug("DELETE /api/lesson-resources/{}", id);
        lessonResourceApi.deleteResource(id);
        return ResponseEntity.ok(AppResponseDto.success());
    }
}
