package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.entity.Lesson;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.mapper.SubmissionMapper;
import com.datn.datnbe.cms.repository.LessonRepository;
import com.datn.datnbe.cms.repository.SubmissionRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService implements SubmissionApi {

    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    // Media uploads for submissions are not handled directly by the CMS module to avoid
    // module dependency on the document module. File uploads are currently unsupported;
    // this keeps the cms module independent and respects modular boundaries.
    private final SubmissionMapper submissionMapper;
    private final SecurityContextUtils securityContextUtils;

    @Override
    public synchronized com.datn.datnbe.cms.dto.response.SubmissionResponseDto createSubmission(String lessonId,
            String content, MultipartFile file) {
    lessonRepository.findById(lessonId)
        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Lesson not found"));

        String userId = securityContextUtils.getCurrentUserId();

        Submission submission = Submission.builder()
                .lessonId(lessonId)
                .studentId(userId)
                .content(content)
                .status("SUBMITTED")
                .build();

        if (file != null && !file.isEmpty()) {
            // Reject file uploads for now to preserve module boundaries.
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "File uploads for submissions are not supported yet.");
        }

        Submission saved = submissionRepository.save(submission);
        return submissionMapper.toDto(saved);
    }

    @Override
    public List<com.datn.datnbe.cms.dto.response.SubmissionResponseDto> getSubmissions(String lessonId) {
        List<Submission> list = submissionRepository.findByLessonId(lessonId);
        return list.stream().map(submissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public com.datn.datnbe.cms.dto.response.SubmissionResponseDto getSubmissionById(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        String currentUser = securityContextUtils.getCurrentUserId();
        if (currentUser.equals(s.getStudentId())) {
            return submissionMapper.toDto(s);
        }

        // check class owner (teacher) permission
        Lesson lesson = lessonRepository.findById(s.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Lesson not found"));

        // Only allow access to the lesson owner
        if (lesson.getOwnerId() != null && lesson.getOwnerId().equals(currentUser)) {
            return submissionMapper.toDto(s);
        }

        throw new AppException(ErrorCode.FORBIDDEN, "You do not have permission to view this submission");
    }

    @Override
    public void deleteSubmission(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));
        submissionRepository.delete(s);
    }
}
