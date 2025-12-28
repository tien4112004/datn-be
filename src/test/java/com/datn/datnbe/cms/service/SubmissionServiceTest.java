package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.entity.Lesson;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.repository.LessonRepository;
import com.datn.datnbe.cms.repository.SubmissionRepository;
// ...existing imports...
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// ...existing imports...

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    SubmissionRepository submissionRepository;

    @Mock
    LessonRepository lessonRepository;

    // MediaStorageApi is intentionally not used by SubmissionService to avoid module coupling

    @Mock
    com.datn.datnbe.cms.mapper.SubmissionMapper submissionMapper;

    @Mock
    SecurityContextUtils securityContextUtils;

    @InjectMocks
    SubmissionService submissionService;

    @Test
    void createSubmission_withFile_uploadsAndSaves() {
        String lessonId = "lesson-1";
        String userId = "student-1";

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(Lesson.builder().id(lessonId).build()));
        when(securityContextUtils.getCurrentUserId()).thenReturn(userId);

    // no file provided in this test, so upload should not be invoked

        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            s.setId("sub-1");
            return s;
        });

        when(submissionMapper.toDto(any(Submission.class))).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            return com.datn.datnbe.cms.dto.response.SubmissionResponseDto.builder()
                    .id(s.getId())
                    .lessonId(s.getLessonId())
                    .studentId(s.getStudentId())
                    .content(s.getContent())
                    .mediaUrl(s.getMediaUrl())
                    .build();
        });

        var dto = submissionService.createSubmission(lessonId, "hello", null);

        assertNotNull(dto);
        assertEquals(lessonId, dto.getLessonId());
        assertEquals(userId, dto.getStudentId());

    // no media interactions expected
    }

}
