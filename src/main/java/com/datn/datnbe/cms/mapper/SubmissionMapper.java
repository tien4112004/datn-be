package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.AnswerDataDto;
import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.entity.answerData.AnswerData;
import com.datn.datnbe.cms.entity.answerData.FillInBlankAnswer;
import com.datn.datnbe.cms.entity.answerData.MultipleChoiceAnswer;
import com.datn.datnbe.cms.entity.answerData.MatchingAnswer;
import com.datn.datnbe.cms.enums.AnswerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SubmissionMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubmissionResponseDto toDto(Submission s) {
        if (s == null)
            return null;
        return SubmissionResponseDto.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .content(s.getContent())
                .mediaUrl(s.getMediaUrl())
                .grade(s.getGrade())
                .status(s.getStatus())
                .postId(s.getPostId())
                .questions(s.getQuestions())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                // New fields
                .assignmentId(s.getAssignmentId())
                .score(s.getScore())
                .maxScore(s.getMaxScore())
                .feedback(s.getFeedback())
                .gradedAt(s.getGradedAt())
                .submittedAt(s.getSubmittedAt())
                // student and gradedByUser will be enriched by service
                .build();
    }

    public Submission toEntity(SubmissionCreateRequest request, String postId) {
        if (request == null)
            return null;
        return Submission.builder().postId(postId).questions(convertAnswers(request.getQuestions())).build();
    }

    private List<AnswerData> convertAnswers(List<AnswerDataDto> dtoList) {
        if (dtoList == null)
            return null;
        return dtoList.stream().map(dto -> {
            AnswerData data = new AnswerData();
            data.setId(dto.getId());

            // Infer answer type from answer object và set type field
            Object answerObj = dto.getAnswer();
            if (answerObj instanceof Map) {
                Map<String, Object> answerMap = (Map<String, Object>) answerObj;
                String typeStr = (String) answerMap.get("type");

                if ("FILL_IN_BLANK".equals(typeStr)) {
                    FillInBlankAnswer fillAnswer = objectMapper.convertValue(answerObj, FillInBlankAnswer.class);
                    fillAnswer.setType(AnswerType.FILL_IN_BLANK);
                    data.setAnswer(fillAnswer);
                } else if ("MULTIPLE_CHOICE".equals(typeStr)) {
                    MultipleChoiceAnswer mcAnswer = objectMapper.convertValue(answerObj, MultipleChoiceAnswer.class);
                    mcAnswer.setType(AnswerType.MULTIPLE_CHOICE);
                    data.setAnswer(mcAnswer);
                } else if ("MATCHING".equals(typeStr)) {
                    MatchingAnswer matchAnswer = objectMapper.convertValue(answerObj, MatchingAnswer.class);
                    matchAnswer.setType(AnswerType.MATCHING);
                    data.setAnswer(matchAnswer);
                } else {
                    data.setAnswer(answerObj);
                }
            } else {
                data.setAnswer(answerObj);
            }

            return data;
        }).collect(Collectors.toList());
    }
}
