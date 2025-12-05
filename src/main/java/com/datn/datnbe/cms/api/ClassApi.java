package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.ClassCollectionRequest;
import com.datn.datnbe.cms.dto.request.ClassCreateRequest;
import com.datn.datnbe.cms.dto.request.ClassUpdateRequest;
import com.datn.datnbe.cms.dto.response.ClassListResponseDto;
import com.datn.datnbe.cms.dto.response.ClassResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface ClassApi {

    ClassResponseDto createClass(ClassCreateRequest request);

    PaginatedResponseDto<ClassListResponseDto> getAllClasses(ClassCollectionRequest request, String teacherId);

    ClassResponseDto getClassById(String id);

    ClassResponseDto updateClass(String id, ClassUpdateRequest request);
}
