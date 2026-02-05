package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.request.SubmissionGradeRequest;
import com.datn.datnbe.cms.dto.request.SubmissionValidationRequest;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionStatisticsDto;
import com.datn.datnbe.cms.dto.response.SubmissionValidationResponse;

import java.util.List;

public interface SubmissionApi {

    SubmissionResponseDto createSubmission(String postId, SubmissionCreateRequest request);

    List<SubmissionResponseDto> getSubmissions(String postId);

    SubmissionResponseDto getSubmissionById(String id);

    void deleteSubmission(String id);

    SubmissionResponseDto gradeSubmissionManually(String submissionId, SubmissionGradeRequest request);

    List<SubmissionResponseDto> getSubmissionsByAssignmentId(String assignmentId);

    List<SubmissionResponseDto> getSubmissionsByAssignmentIdAndStudentId(String assignmentId, String studentId);

    SubmissionStatisticsDto getSubmissionStatistics(String postId);

    SubmissionStatisticsDto getAssignmentStatistics(String assignmentId);

    SubmissionValidationResponse validateSubmission(String assignmentId, SubmissionValidationRequest request);
}
