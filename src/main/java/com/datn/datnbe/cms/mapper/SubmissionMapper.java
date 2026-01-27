package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.AnswerDataDto;
import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.entity.answerData.AnswerData;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

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
                .postId(s.getPostId())
                .questions(s.getQuestions())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    public Submission toEntity(SubmissionCreateRequest request, String postId) {
        if (request == null)
            return null;
        return Submission.builder()
                .postId(postId)
                .studentId(request.getStudentId())
                .questions(convertAnswers(request.getQuestions()))
                .build();
    }

    private List<AnswerData> convertAnswers(List<AnswerDataDto> dtoList) {
        if (dtoList == null)
            return null;
        return dtoList.stream()
                .map(dto -> {
                    AnswerData data = new AnswerData();
                    data.setId(dto.getId());
                    data.setType(dto.getType());
                    data.setAnswer(dto.getAnswer());
                    return data;
                })
                .collect(Collectors.toList());
    }
}
