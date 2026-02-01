package com.datn.datnbe.document.integration;

import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.datn.datnbe.document.exam.dto.DimensionTopicDto;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.MatrixDimensionsDto;
import com.datn.datnbe.document.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.document.exam.dto.response.ExamDraftDto;
import com.datn.datnbe.document.exam.enums.MissingQuestionStrategy;
import com.datn.datnbe.document.exam.repository.ExamQuestionRepository;
import com.datn.datnbe.document.exam.service.QuestionSelectionService;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionSelectionServiceTest extends BaseIntegrationTest {

    @Autowired
    private QuestionSelectionService questionSelectionService;

    @Autowired
    private ExamQuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
    }

    @Test
    void selectQuestionsForMatrix_HappyPath_returnsExactQuestions() {
        // Arrange
        String subject = "MATH101";
        String topicName = "Algebra";

        // Insert enough questions
        createQuestions(subject, topicName, Difficulty.KNOWLEDGE, QuestionType.MULTIPLE_CHOICE, 5);

        // Build request
        GenerateExamFromMatrixRequest request = buildRequest(subject, topicName, 3, 1.0);

        // Act
        ExamDraftDto result = questionSelectionService.selectQuestionsForMatrix(request, UUID.randomUUID());

        // Assert
        assertThat(result.getSelectedQuestions()).hasSize(3);
        assertThat(result.getMissingQuestions()).isEmpty();
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    void selectQuestionsForMatrix_NotEnoughQuestions_returnsGaps() {
        // Arrange
        String subject = "MATH101";
        String topicName = "Algebra";

        // Insert fewer questions than needed (needed 5, have 2)
        createQuestions(subject, topicName, Difficulty.KNOWLEDGE, QuestionType.MULTIPLE_CHOICE, 2);

        // Build request: Demand 5 Knowledge/MCQ questions
        ExamMatrixDto matrix = createSimpleMatrix(topicName, "easy", "multiple_choice", 5, 10.0);
        GenerateExamFromMatrixRequest request = GenerateExamFromMatrixRequest.builder()
                .subject(subject)
                .matrix(matrix)
                .missingStrategy(MissingQuestionStrategy.ALLOW_PARTIAL)
                .build();

        // Act
        ExamDraftDto result = questionSelectionService.selectQuestionsForMatrix(request, UUID.randomUUID());

        // Assert
        assertThat(result.getSelectedQuestions()).hasSize(2);
        assertThat(result.getMissingQuestions()).hasSize(1);
        assertThat(result.getMissingQuestions().get(0).getRequiredCount()).isEqualTo(5);
        assertThat(result.getMissingQuestions().get(0).getAvailableCount()).isEqualTo(2);
        assertThat(result.isComplete()).isFalse();
    }

    @Test
    void selectQuestionsForMatrix_FailFastStrategy_throwsException() {
        // Arrange
        String subject = "MATH101";
        String topicName = "Algebra";
        createQuestions(subject, topicName, Difficulty.KNOWLEDGE, QuestionType.MULTIPLE_CHOICE, 0);

        ExamMatrixDto matrix = createSimpleMatrix(topicName, "easy", "multiple_choice", 5, 10.0);
        GenerateExamFromMatrixRequest request = GenerateExamFromMatrixRequest.builder()
                .subject(subject)
                .matrix(matrix)
                .missingStrategy(MissingQuestionStrategy.FAIL_FAST)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> questionSelectionService.selectQuestionsForMatrix(request, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fulfill all matrix requirements");
    }

    // --- Helpers ---

    private void createQuestions(String subject, String chapter, Difficulty diff, QuestionType type, int count) {
        List<QuestionBankItem> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            questions.add(QuestionBankItem.builder()
                    .id(UUID.randomUUID().toString())
                    .subject(subject)
                    .chapter(chapter)
                    .difficulty(diff)
                    .type(type)
                    .title("Question " + i)
                    .data("Sample Data")
                    .build());
        }
        questionRepository.saveAll(questions);
    }

    private GenerateExamFromMatrixRequest buildRequest(String subject, String topic, int count, double points) {
        ExamMatrixDto matrix = createSimpleMatrix(topic, "easy", "multiple_choice", count, points);
        return GenerateExamFromMatrixRequest.builder()
                .subject(subject)
                .matrix(matrix)
                .timeLimitMinutes(60)
                .missingStrategy(MissingQuestionStrategy.ALLOW_PARTIAL) // Default
                .build();
    }

    private ExamMatrixDto createSimpleMatrix(String topic, String diff, String type, int count, double points) {
        // Create Dimensions
        MatrixDimensionsDto dimensions = new MatrixDimensionsDto();
        dimensions.setTopics(List.of(new DimensionTopicDto(topic, 100)));
        dimensions.setDifficulties(List.of(diff));
        dimensions.setQuestionTypes(List.of(type));

        // Create Matrix Data: 1 topic x 1 diff x 1 type
        List<List<List<String>>> matrixData = new ArrayList<>();
        List<List<String>> topicRow = new ArrayList<>();
        List<String> diffRow = new ArrayList<>();
        diffRow.add(count + ":" + points); // "count:points"
        topicRow.add(diffRow);
        matrixData.add(topicRow);

        ExamMatrixDto dto = new ExamMatrixDto();
        dto.setDimensions(dimensions);
        dto.setMatrix(matrixData);
        return dto;
    }
}
