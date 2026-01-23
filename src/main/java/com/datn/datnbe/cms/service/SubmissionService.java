package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.mapper.SubmissionMapper;
import com.datn.datnbe.cms.repository.SubmissionRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService implements SubmissionApi {

    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final SecurityContextUtils securityContextUtils;
    private final PostApi postApi;

    @Override
    public synchronized SubmissionResponseDto createSubmission(String postId,
            String studentId) {
        Submission submission = Submission.builder().postId(postId).studentId(studentId).build();

        Submission saved = submissionRepository.save(submission);
        return submissionMapper.toDto(saved);
    }

    @Override
    public List<SubmissionResponseDto> getSubmissions(String postId) {
        List<Submission> list = submissionRepository.findByPostId(postId);
        return list.stream().map(submissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public SubmissionResponseDto getSubmissionById(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        String currentUser = securityContextUtils.getCurrentUserId();

        PostResponseDto post = postApi.getPostById(s.getPostId());
    
        if (currentUser.equals(post.getAuthorId()) || currentUser.equals(s.getStudentId())) {
            return submissionMapper.toDto(s);
        }

        throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found");
    }

    @Override
    public void deleteSubmission(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));
        submissionRepository.delete(s);
    }
}
