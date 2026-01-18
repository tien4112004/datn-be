package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.document.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface AssignmentApi {

    AssignmentResponse createAssignment(AssignmentCreateRequest request);

    PaginatedResponseDto<AssignmentResponse> getAssignments(int page, int size, String search);

    AssignmentResponse getAssignmentById(String id);

    AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request);

    void deleteAssignment(String id);
}
