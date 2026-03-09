package com.datn.datnbe.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.datn.datnbe.document.dto.pdf.AssignmentPdfViewModel;
import com.datn.datnbe.document.dto.pdf.PdfContextBlock;
import com.datn.datnbe.document.dto.pdf.PdfQuestionViewModel;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
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
    void contextBlock_groupsLinkedQuestionsUnderCorrectContext() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        assertThat(vm.getContextBlocks()).hasSize(1);

        PdfContextBlock block = vm.getContextBlocks().get(0);
        assertThat(block.getId()).isEqualTo("mmc693oc-me19l2bjx");
        assertThat(block.getTitle()).isEqualTo("Linda's Introduction");
        assertThat(block.getContent()).contains("My name is Linda");

        // 3 questions reference this contextId
        assertThat(block.getQuestions()).hasSize(3);
    }

    @Test
    void standaloneQuestions_containsQuestionsWithNoContextId() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        // MULTIPLE_CHOICE (no ctx) + FILL_IN_BLANK (no ctx) + MATCHING (no ctx) = 3
        assertThat(vm.getStandaloneQuestions()).hasSize(3);
    }

    @Test
    void questionNumbering_isSequentialAndGapless() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        List<PdfQuestionViewModel> allQuestions = collectAllQuestionsInDocumentOrder(vm);

        assertThat(allQuestions).hasSize(6);
        for (int i = 0; i < allQuestions.size(); i++) {
            assertThat(allQuestions.get(i).getNumber()).isEqualTo(i + 1);
        }
    }

    @Test
    void multipleChoiceData_isCastCorrectly() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        PdfQuestionViewModel mc = vm.getStandaloneQuestions()
                .stream()
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

        PdfQuestionViewModel fib = vm.getStandaloneQuestions()
                .stream()
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

        PdfQuestionViewModel oe = vm.getContextBlocks()
                .get(0)
                .getQuestions()
                .stream()
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

        PdfQuestionViewModel matching = vm.getStandaloneQuestions()
                .stream()
                .filter(q -> q.getType() == QuestionType.MATCHING)
                .findFirst()
                .orElseThrow();

        assertThat(matching.getMatchingData()).isNotNull();
        assertThat(matching.getMatchingData()).isInstanceOf(MatchingData.class);
        assertThat(matching.getMatchingData().getPairs()).hasSize(3);
    }

    @Test
    void contextBlocksAppearInOrderOfFirstLinkedQuestion() {
        AssignmentPdfViewModel vm = mapper.toViewModel(sampleAssignment);

        // Only one context block in sample — verify it's the one with Linda's Introduction
        assertThat(vm.getContextBlocks().get(0).getId()).isEqualTo("mmc693oc-me19l2bjx");
    }

    // --- helpers ---

    /**
     * Collects questions in the order they appear in the document:
     * standalone questions first (in original list order), then context-block questions.
     * This mirrors how the mapper assigns sequential numbers.
     */
    private List<PdfQuestionViewModel> collectAllQuestionsInDocumentOrder(AssignmentPdfViewModel vm) {
        // The mapper numbers questions in original input list order, regardless of
        // standalone vs context grouping, so we need to merge and sort by number.
        List<PdfQuestionViewModel> all = new java.util.ArrayList<>();
        all.addAll(vm.getStandaloneQuestions());
        vm.getContextBlocks().forEach(b -> all.addAll(b.getQuestions()));
        all.sort(java.util.Comparator.comparingInt(PdfQuestionViewModel::getNumber));
        return all;
    }
}
