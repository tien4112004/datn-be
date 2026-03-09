package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.pdf.AssignmentPdfViewModel;
import com.datn.datnbe.document.dto.pdf.PdfContextBlock;
import com.datn.datnbe.document.dto.pdf.PdfQuestionViewModel;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.entity.AssignmentContext;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentPdfViewModelMapper {

    private final ObjectMapper objectMapper;

    public AssignmentPdfViewModel toViewModel(AssignmentResponse assignment) {
        Map<String, AssignmentContext> contextById = buildContextMap(assignment.getContexts());

        // Preserve insertion order — context blocks appear in order of their
        // first linked question in the questions list.
        Map<String, PdfContextBlock> contextBlocks = new LinkedHashMap<>();
        List<PdfQuestionViewModel> standaloneQuestions = new ArrayList<>();

        List<Question> questions = assignment.getQuestions() != null ? assignment.getQuestions() : List.of();

        int number = 1;
        for (Question question : questions) {
            PdfQuestionViewModel qvm = toQuestionViewModel(question, number++);
            String contextId = question.getContextId();

            if (contextId != null && contextById.containsKey(contextId)) {
                contextBlocks.computeIfAbsent(contextId, id -> buildContextBlock(contextById.get(id)))
                        .getQuestions()
                        .add(qvm);
            } else {
                standaloneQuestions.add(qvm);
            }
        }

        double totalPoints = questions.stream().mapToDouble(q -> q.getPoint() != null ? q.getPoint() : 0.0).sum();

        return AssignmentPdfViewModel.builder()
                .title(assignment.getTitle())
                .subject(assignment.getSubject())
                .grade(assignment.getGrade())
                .chapter(assignment.getChapter())
                .description(assignment.getDescription())
                .totalPoints(totalPoints)
                .contextBlocks(new ArrayList<>(contextBlocks.values()))
                .standaloneQuestions(standaloneQuestions)
                .build();
    }

    private Map<String, AssignmentContext> buildContextMap(List<AssignmentContext> contexts) {
        if (contexts == null)
            return Map.of();
        return contexts.stream().collect(Collectors.toMap(AssignmentContext::getId, c -> c));
    }

    private PdfContextBlock buildContextBlock(AssignmentContext context) {
        return PdfContextBlock.builder()
                .id(context.getId())
                .title(context.getTitle())
                .content(context.getContent())
                .author(context.getAuthor())
                .questions(new ArrayList<>())
                .build();
    }

    private PdfQuestionViewModel toQuestionViewModel(Question question, int number) {
        PdfQuestionViewModel.PdfQuestionViewModelBuilder builder = PdfQuestionViewModel.builder()
                .number(number)
                .title(question.getTitle())
                .type(question.getType())
                .point(question.getPoint());

        if (question.getType() == null || question.getData() == null) {
            return builder.build();
        }

        switch (question.getType()) {
            case MULTIPLE_CHOICE -> builder.multipleChoiceData(convert(question.getData(), MultipleChoiceData.class));
            case FILL_IN_BLANK -> builder.fillInBlankData(convert(question.getData(), FillInBlankData.class));
            case OPEN_ENDED -> builder.openEndedData(convert(question.getData(), OpenEndedData.class));
            case MATCHING -> builder.matchingData(convert(question.getData(), MatchingData.class));
        }

        return builder.build();
    }

    /**
     * Converts question data to the target type.
     * Uses ObjectMapper.convertValue() to handle both the case where Jackson
     * has already deserialized to the concrete type and the edge case where it
     * produced a LinkedHashMap (e.g., from certain JSONB deserialization paths).
     */
    private <T> T convert(Object data, Class<T> targetType) {
        try {
            return objectMapper.convertValue(data, targetType);
        } catch (Exception e) {
            log.warn("Could not convert question data to {}: {}", targetType.getSimpleName(), e.getMessage());
            return null;
        }
    }
}
