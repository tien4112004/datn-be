package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;

import java.util.List;

public interface SubmissionApi {

    SubmissionResponseDto createSubmission(String postId, String studentId);

    List<SubmissionResponseDto> getSubmissions(String postId);

    SubmissionResponseDto getSubmissionById(String id);

    void deleteSubmission(String id);
}
