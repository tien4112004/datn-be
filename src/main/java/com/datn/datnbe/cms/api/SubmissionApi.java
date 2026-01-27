package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.request.SubmissionGradeRequest;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;

import java.util.List;

public interface SubmissionApi {

    SubmissionResponseDto createSubmission(String postId, SubmissionCreateRequest request);

    List<SubmissionResponseDto> getSubmissions(String postId);

    SubmissionResponseDto getSubmissionById(String id);

    void deleteSubmission(String id);

    SubmissionResponseDto gradeSubmissionManually(String submissionId, SubmissionGradeRequest request);
}
