package com.datn.datnbe.student.exam.service;

import com.datn.datnbe.student.exam.dto.*;
import com.datn.datnbe.student.exam.dto.request.GenerateExamFromMatrixRequest;
import com.datn.datnbe.student.exam.dto.response.ExamDraftDto;
import com.datn.datnbe.student.exam.dto.response.ExamQuestionDto;
import com.datn.datnbe.student.exam.dto.response.MatrixGapDto;
import com.datn.datnbe.student.exam.entity.Question;
import com.datn.datnbe.student.exam.enums.ExamDifficulty;
import com.datn.datnbe.student.exam.enums.MissingQuestionStrategy;
import com.datn.datnbe.student.exam.enums.QuestionType;
import com.datn.datnbe.student.exam.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for selecting questions from the question bank based on exam matrix criteria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class QuestionSelectionService {

    QuestionRepository questionRepository;

    /**
     * Select questions from the question bank to fulfill matrix requirements.
     *
     * @param request   The request containing the matrix and configuration
     * @param teacherId The ID of the teacher (for personal questions)
     * @return ExamDraftDto containing selected questions and any gaps
     */
    public ExamDraftDto selectQuestionsForMatrix(GenerateExamFromMatrixRequest request, UUID teacherId) {
        ExamMatrixV2Dto matrix = request.getMatrix();

        List<ExamQuestionDto> selectedQuestions = new ArrayList<>();
        List<MatrixGapDto> gaps = new ArrayList<>();
        AtomicInteger orderIndex = new AtomicInteger(1);

        MatrixDimensionsDto dimensions = matrix.getDimensions();
        List<DimensionTopicDto> topics = dimensions.getTopics();
        List<String> difficulties = dimensions.getDifficulties();
        List<String> questionTypes = dimensions.getQuestionTypes();

        // Track selected question IDs to avoid duplicates
        Set<UUID> selectedIds = new HashSet<>();

        // Iterate through the 3D matrix
        for (int topicIdx = 0; topicIdx < topics.size(); topicIdx++) {
            DimensionTopicDto topic = topics.get(topicIdx);
            List<List<List<Number>>> difficultyRows = matrix.getMatrix().get(topicIdx);

            for (int diffIdx = 0; diffIdx < difficulties.size(); diffIdx++) {
                String difficulty = difficulties.get(diffIdx);
                List<List<Number>> questionTypeCells = difficultyRows.get(diffIdx);

                for (int qtypeIdx = 0; qtypeIdx < questionTypes.size(); qtypeIdx++) {
                    List<Number> cell = questionTypeCells.get(qtypeIdx);
                    String questionType = questionTypes.get(qtypeIdx);

                    // cell format: [count, points]
                    int count = cell != null && !cell.isEmpty() ? cell.get(0).intValue() : 0;
                    double points = cell != null && cell.size() > 1 ? cell.get(1).doubleValue() : 0.0;

                    if (count > 0) {
                        // Select questions for this cell
                        SelectionResult result = selectQuestionsForCell(topic.getName(),
                                difficulty,
                                questionType,
                                count,
                                points,
                                teacherId,
                                request.getIncludePersonalQuestions(),
                                selectedIds,
                                orderIndex);

                        selectedQuestions.addAll(result.questions);
                        selectedIds.addAll(result.selectedIds);

                        // Check if there's a gap
                        if (result.questions.size() < count) {
                            gaps.add(MatrixGapDto.builder()
                                    .topic(topic.getName())
                                    .difficulty(difficulty)
                                    .questionType(questionType)
                                    .requiredCount(count)
                                    .availableCount(result.questions.size())
                                    .build());
                        }
                    }
                }
            }
        }

        // Handle missing questions based on strategy
        if (!gaps.isEmpty() && request.getMissingStrategy() == MissingQuestionStrategy.FAIL_FAST) {
            throw new IllegalStateException(
                    "Cannot fulfill all matrix requirements. Missing questions for: " + gaps.stream()
                            .map(g -> g.getTopic() + "/" + g.getDifficulty() + "/" + g.getQuestionType())
                            .toList());
        }

        // Calculate totals
        double totalPoints = selectedQuestions.stream()
                .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 0)
                .sum();

        return ExamDraftDto.builder()
                .examId(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription())
                .selectedQuestions(selectedQuestions)
                .missingQuestions(gaps)
                .totalPoints(totalPoints)
                .totalQuestions(selectedQuestions.size())
                .isComplete(gaps.isEmpty())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .build();
    }

    /**
     * Select questions for a specific matrix cell.
     */
    private SelectionResult selectQuestionsForCell(String topicName,
            String difficultyStr,
            String questionTypeStr,
            int requiredCount,
            double pointsPerCell,
            UUID teacherId,
            boolean includePersonal,
            Set<UUID> alreadySelected,
            AtomicInteger orderIndex) {
        ExamDifficulty difficulty = parseExamDifficulty(difficultyStr);
        QuestionType questionType = parseQuestionType(questionTypeStr);

        // Calculate points per question
        double pointsPerQuestion = requiredCount > 0 ? pointsPerCell / requiredCount : 0;

        // Fetch more questions than needed to filter out already selected ones
        int fetchLimit = requiredCount * 3;

        List<Question> candidates;
        if (includePersonal && teacherId != null) {
            candidates = questionRepository.findMatchingQuestionsForMatrix(teacherId,
                    topicName,
                    difficulty,
                    questionType,
                    PageRequest.of(0, fetchLimit));
        } else {
            candidates = questionRepository
                    .findPublicMatchingQuestions(topicName, difficulty, questionType, PageRequest.of(0, fetchLimit));
        }

        // Filter out already selected and take required count
        List<ExamQuestionDto> selected = new ArrayList<>();
        Set<UUID> newlySelectedIds = new HashSet<>();

        for (Question q : candidates) {
            if (selected.size() >= requiredCount) {
                break;
            }
            if (!alreadySelected.contains(q.getQuestionId()) && !newlySelectedIds.contains(q.getQuestionId())) {
                selected.add(mapToExamQuestionDto(q, pointsPerQuestion, orderIndex.getAndIncrement()));
                newlySelectedIds.add(q.getQuestionId());
            }
        }

        log.debug("Selected {}/{} questions for topic={}, difficulty={}, type={}",
                selected.size(),
                requiredCount,
                topicName,
                difficultyStr,
                questionTypeStr);

        return new SelectionResult(selected, newlySelectedIds);
    }

    /**
     * Map a Question entity to ExamQuestionDto.
     */
    private ExamQuestionDto mapToExamQuestionDto(Question question, double points, int orderIndex) {
        return ExamQuestionDto.builder()
                .questionId(question.getQuestionId())
                .content(question.getContent())
                .questionType(question.getQuestionType())
                .topic(question.getTopic())
                .difficulty(question.getDifficulty())
                .points(points)
                .orderIndex(orderIndex)
                .answers(question.getAnswers())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    /**
     * Parse difficulty string to enum.
     */
    private ExamDifficulty parseExamDifficulty(String difficulty) {
        if (difficulty == null)
            return null;
        try {
            return ExamDifficulty.valueOf(difficulty.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown difficulty: {}", difficulty);
            return null;
        }
    }

    /**
     * Parse question type string to enum.
     */
    private QuestionType parseQuestionType(String questionType) {
        if (questionType == null)
            return null;
        try {
            return QuestionType.valueOf(questionType.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown question type: {}", questionType);
            return null;
        }
    }

    /**
     * Internal class to hold selection results.
     */
    private record SelectionResult(List<ExamQuestionDto> questions, Set<UUID> selectedIds) {
    }
}
