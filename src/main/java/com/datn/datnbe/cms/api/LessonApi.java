package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.LessonCreateRequest;
import com.datn.datnbe.cms.dto.request.LessonUpdateRequest;
import com.datn.datnbe.cms.dto.response.LessonResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface LessonApi {

    LessonResponseDto createLesson(String classId, LessonCreateRequest request);

    PaginatedResponseDto<LessonResponseDto> getLessons(String classId, int page, int size, String search);

    LessonResponseDto getLessonById(String id);

    LessonResponseDto updateLesson(String id, LessonUpdateRequest request);

    void deleteLesson(String id);
}
