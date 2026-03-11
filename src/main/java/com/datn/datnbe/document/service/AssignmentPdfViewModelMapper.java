package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.pdf.AssignmentPdfViewModel;
import com.datn.datnbe.document.dto.pdf.PdfContextBlock;
import com.datn.datnbe.document.dto.pdf.PdfQuestionViewModel;
import com.datn.datnbe.document.dto.pdf.PdfSection;
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

        // Build sections in first-occurrence order, matching the frontend
        // groupQuestionsByContext logic: context groups and standalone questions
        // are interleaved based on when they first appear in the original list.
        List<PdfSection> sections = new ArrayList<>();
        Map<String, PdfSection> contextSections = new LinkedHashMap<>();

        List<Question> questions = assignment.getQuestions() != null ? assignment.getQuestions() : List.of();

        for (Question question : questions) {
            PdfQuestionViewModel qvm = toQuestionViewModel(question, 0);
            String contextId = question.getContextId();

            if (contextId != null && contextById.containsKey(contextId)) {
                PdfSection section = contextSections.get(contextId);
                if (section == null) {
                    section = PdfSection.builder()
                            .type("context")
                            .contextBlock(buildContextBlock(contextById.get(contextId)))
                            .questions(new ArrayList<>())
                            .build();
                    contextSections.put(contextId, section);
                    sections.add(section);
                }
                section.getQuestions().add(qvm);
            } else {
                sections.add(PdfSection.builder().type("standalone").questions(new ArrayList<>(List.of(qvm))).build());
            }
        }

        // Number questions sequentially in section order
        int number = 1;
        for (PdfSection section : sections) {
            for (PdfQuestionViewModel qvm : section.getQuestions()) {
                qvm.setNumber(number++);
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
                .sections(sections)
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
