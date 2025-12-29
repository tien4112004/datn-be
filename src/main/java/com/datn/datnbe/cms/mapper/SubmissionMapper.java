package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.entity.Submission;
import org.springframework.stereotype.Component;

@Component
public class SubmissionMapper {

    public SubmissionResponseDto toDto(Submission s) {
        if (s == null)
            return null;
        return SubmissionResponseDto.builder()
                .id(s.getId())
                .lessonId(s.getLessonId())
                .studentId(s.getStudentId())
                .content(s.getContent())
                .mediaUrl(s.getMediaUrl())
                .grade(s.getGrade())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
