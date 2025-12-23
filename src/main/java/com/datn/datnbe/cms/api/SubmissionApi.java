package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionApi {

    SubmissionResponseDto createSubmission(String lessonId, String content, MultipartFile file);

    List<SubmissionResponseDto> getSubmissions(String lessonId);

    SubmissionResponseDto getSubmissionById(String id);

    void deleteSubmission(String id);
}
