package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.cms.dto.request.ClassCollectionRequest;
import com.datn.datnbe.cms.dto.request.ClassCreateRequest;
import com.datn.datnbe.cms.dto.request.ClassUpdateRequest;
import com.datn.datnbe.cms.dto.response.ClassListResponseDto;
import com.datn.datnbe.cms.dto.response.ClassResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

import java.util.List;

public interface ClassApi {

    ClassResponseDto createClass(ClassCreateRequest request);

    PaginatedResponseDto<ClassListResponseDto> getAllClasses(ClassCollectionRequest request, String ownerId);

    ClassResponseDto getClassById(String id);

    ClassResponseDto updateClass(String id, ClassUpdateRequest request);

    void deleteClass(String id);

    List<LinkedResourceDto> getClassResources(String classId);
}
