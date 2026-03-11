package com.datn.datnbe.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.datn.datnbe.document.dto.pdf.AssignmentPdfViewModel;
import com.datn.datnbe.document.dto.pdf.PdfQuestionViewModel;
import com.datn.datnbe.document.dto.pdf.PdfSection;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignmentPdfViewModelMapperTest {

    private AssignmentPdfViewModelMapper mapper;
    private AssignmentResponse sampleAssignment;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        mapper = new AssignmentPdfViewModelMapper(objectMapper);

        try (InputStream is = getClass().getResourceAsStream("/fixtures/sample-assignment.json")) {
            sampleAssignment = objectMapper.readValue(is, AssignmentResponse.class);
        }
    }

    @Test
    void topLevelFields_areMappedCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        assertThat(vm.getTitle()).isEqualTo("Bài tập về My house");
        assertThat(vm.getSubject()).isEqualTo("TA");
        assertThat(vm.getGrade()).isEqualTo("3");
    }

    @Test
    void totalPoints_isSumOfAllQuestionPoints() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        // 6 questions × 10.0 points each
        assertThat(vm.getTotalPoints()).isEqualTo(60.0);
    }

    @Test
    void sections_preserveFirstOccurrenceOrder() {
        // Sample data order: MC(standalone), FIB(standalone), MC(ctx), OE(ctx), OE(ctx), Matching(standalone)
        // Expected sections: standalone, standalone, context(3 questions), standalone
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        assertThat(vm.getSections()).hasSize(4);
        assertThat(vm.getSections().get(0).getType()).isEqualTo("standalone");
        assertThat(vm.getSections().get(1).getType()).isEqualTo("standalone");
        assertThat(vm.getSections().get(2).getType()).isEqualTo("context");
        assertThat(vm.getSections().get(3).getType()).isEqualTo("standalone");
    }

    @Test
    void contextSection_groupsLinkedQuestionsUnderCorrectContext() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfSection contextSection = vm.getSections()
                .stream()
                .filter(s -> "context".equals(s.getType()))
                .findFirst()
                .orElseThrow();

        assertThat(contextSection.getContextBlock()).isNotNull();
        assertThat(contextSection.getContextBlock().getId()).isEqualTo("mmc693oc-me19l2bjx");
        assertThat(contextSection.getContextBlock().getTitle()).isEqualTo("Linda's Introduction");
        assertThat(contextSection.getContextBlock().getContent()).contains("My name is Linda");

        // 3 questions reference this contextId
        assertThat(contextSection.getQuestions()).hasSize(3);
    }

    @Test
    void standaloneSections_containQuestionsWithNoContextId() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        List<PdfSection> standaloneSections = vm.getSections()
                .stream()
                .filter(s -> "standalone".equals(s.getType()))
                .toList();

        // MC (no ctx) + FIB (no ctx) + Matching (no ctx) = 3 standalone sections
        assertThat(standaloneSections).hasSize(3);
        // Each standalone section has exactly 1 question
        standaloneSections.forEach(s -> assertThat(s.getQuestions()).hasSize(1));
    }

    @Test
    void questionNumbering_isSequentialAndMatchesSectionOrder() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        List<PdfQuestionViewModel> allQuestions = collectAllQuestionsInSectionOrder(vm);

        assertThat(allQuestions).hasSize(6);
        for (int i = 0; i < allQuestions.size(); i++) {
            assertThat(allQuestions.get(i).getNumber()).isEqualTo(i + 1);
        }
    }

    @Test
    void multipleChoiceData_isCastCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfQuestionViewModel mc = collectAllQuestionsInSectionOrder(vm).stream()
                .filter(q -> q.getType() == QuestionType.MULTIPLE_CHOICE)
                .findFirst()
                .orElseThrow();

        assertThat(mc.getMultipleChoiceData()).isNotNull();
        assertThat(mc.getMultipleChoiceData()).isInstanceOf(MultipleChoiceData.class);
        assertThat(mc.getMultipleChoiceData().getOptions()).hasSize(4);
        assertThat(mc.getMultipleChoiceData().getOptions())
                .anyMatch(o -> Boolean.TRUE.equals(o.getIsCorrect()) && "Bedroom".equals(o.getText()));
    }

    @Test
    void fillInBlankData_isCastCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfQuestionViewModel fib = collectAllQuestionsInSectionOrder(vm).stream()
                .filter(q -> q.getType() == QuestionType.FILL_IN_BLANK)
                .findFirst()
                .orElseThrow();

        assertThat(fib.getFillInBlankData()).isNotNull();
        assertThat(fib.getFillInBlankData()).isInstanceOf(FillInBlankData.class);
        assertThat(fib.getFillInBlankData().getSegments()).hasSize(3);
    }

    @Test
    void openEndedData_isCastCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfQuestionViewModel oe = collectAllQuestionsInSectionOrder(vm).stream()
                .filter(q -> q.getType() == QuestionType.OPEN_ENDED)
                .findFirst()
                .orElseThrow();

        assertThat(oe.getOpenEndedData()).isNotNull();
        assertThat(oe.getOpenEndedData()).isInstanceOf(OpenEndedData.class);
        assertThat(oe.getOpenEndedData().getMaxLength()).isEqualTo(100);
    }

    @Test
    void matchingData_isCastCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfQuestionViewModel matching = collectAllQuestionsInSectionOrder(vm).stream()
                .filter(q -> q.getType() == QuestionType.MATCHING)
                .findFirst()
                .orElseThrow();

        assertThat(matching.getMatchingData()).isNotNull();
        assertThat(matching.getMatchingData()).isInstanceOf(MatchingData.class);
        assertThat(matching.getMatchingData().getPairs()).hasSize(3);
    }

    // --- helpers ---

    /**
     * Collects questions in section order — the same order rendered in the PDF
     * and matching the frontend Question List tab.
     */
    private List<PdfQuestionViewModel> collectAllQuestionsInSectionOrder(AssignmentPdfViewModel vm) {
        List<PdfQuestionViewModel> all = new ArrayList<>();
        vm.getSections().forEach(s -> all.addAll(s.getQuestions()));
        return all;
    }
}
