package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface SubmissionApi {

    SubmissionResponseDto createSubmission(String lessonId, String content, MultipartFile file);

    List<SubmissionResponseDto> getSubmissions(String lessonId);

    SubmissionResponseDto createSubmission(String postId, String studentId);

    List<SubmissionResponseDto> getSubmissionsV2(String postId);

    SubmissionResponseDto getSubmissionByIdV2(String id);

    SubmissionResponseDto getSubmissionById(String id);

    void deleteSubmission(String id);
}
