package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.LessonResourceCreateRequest;
import com.datn.datnbe.cms.dto.response.LessonResourceResponseDto;
import java.util.List;

public interface LessonResourceApi {

    LessonResourceResponseDto createResource(String lessonId, LessonResourceCreateRequest request);

    List<LessonResourceResponseDto> getResources(String lessonId);

    LessonResourceResponseDto getResourceById(String id);

    LessonResourceResponseDto updateResource(String id, LessonResourceCreateRequest request);

    void deleteResource(String id);
}
