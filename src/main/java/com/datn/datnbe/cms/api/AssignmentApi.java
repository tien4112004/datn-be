package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.AddQuestionRequest;
import com.datn.datnbe.cms.dto.request.AssignmentCreateRequest;
import com.datn.datnbe.cms.dto.request.AssignmentUpdateRequest;
import com.datn.datnbe.cms.dto.response.AssignmentQuestionInfo;
import com.datn.datnbe.cms.dto.response.AssignmentResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

import java.util.List;

public interface AssignmentApi {

    AssignmentResponse createAssignment(AssignmentCreateRequest request);

    PaginatedResponseDto<AssignmentResponse> getAssignments(int page, int size, String search);

    AssignmentResponse getAssignmentById(String id);

    AssignmentResponse updateAssignment(String id, AssignmentUpdateRequest request);

    void deleteAssignment(String id);

    AssignmentQuestionInfo addQuestionToAssignment(String assignmentId, AddQuestionRequest request);

    void removeQuestionFromAssignment(String assignmentId, String questionId);

    List<AssignmentQuestionInfo> getAssignmentQuestions(String assignmentId);
}
