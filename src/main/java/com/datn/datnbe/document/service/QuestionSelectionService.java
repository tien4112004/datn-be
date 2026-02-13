package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.*;
import com.datn.datnbe.document.dto.request.GenerateAssignmentFromMatrixRequest;
import com.datn.datnbe.document.dto.response.AssignmentDraftDto;
import com.datn.datnbe.document.dto.response.MatrixGapDto;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.datn.datnbe.document.enums.MissingQuestionStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for selecting questions from the question bank based on exam matrix criteria.
 * Optimized with window functions to reduce database round trips.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class QuestionSelectionService {

    EntityManager entityManager;

    /**
     * Select questions from the question bank to fulfill matrix requirements.
     * Optimized implementation using window functions.
     *
     * @param request   The request containing the matrix and configuration
     * @param teacherId The ID of the teacher (for personal questions)
     * @return AssignmentDraftDto containing selected questions and any gaps
     */
    @Transactional(readOnly = true)
    public AssignmentDraftDto selectQuestionsForMatrix(GenerateAssignmentFromMatrixRequest request, String teacherId) {
        AssignmentMatrixDto matrix = request.getMatrix();
        MatrixDimensionsDto dimensions = matrix.getDimensions();
        MatrixMetadataDto metadata = matrix.getMetadata();

        // Extract filtering parameters from metadata
        String grade = metadata != null ? metadata.getGrade() : null;
        String subject = metadata != null && metadata.getSubject() != null
                ? metadata.getSubject()
                : request.getSubject();

        // Build selection criteria from matrix
        List<SelectionCriteria> criteriaList = buildSelectionCriteria(matrix, dimensions);

        log.info("Built {} selection criteria from matrix for subject '{}', grade '{}'",
                criteriaList.size(),
                subject,
                grade);
        for (SelectionCriteria criteria : criteriaList) {
            log.info(
                    "  → Criteria: topic='{}', subtopics={}, difficulty='{}' ({}), type='{}' ({}), count={}, points={}",
                    criteria.topicName,
                    criteria.subtopicNames,
                    criteria.difficultyStr,
                    criteria.difficulty,
                    criteria.questionTypeStr,
                    criteria.questionType,
                    criteria.requiredCount,
                    criteria.pointsPerCell);
        }

        if (criteriaList.isEmpty()) {
            return AssignmentDraftDto.builder()
                    .id(UUID.randomUUID().toString())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .subject(subject)
                    .grade(grade)
                    .duration(request.getTimeLimitMinutes())
                    .questions(List.of())
                    .missingQuestions(List.of())
                    .totalPoints(0.0)
                    .totalQuestions(0)
                    .isComplete(true)
                    .build();
        }

        // Execute optimized query
        List<QuestionBankItem> selectedQuestions = selectQuestionsWithWindowFunction(subject,
                grade,
                criteriaList,
                teacherId,
                request.getIncludePersonalQuestions());

        // Map results and check for gaps
        Map<String, List<QuestionBankItem>> groupedResults = groupResultsByCriteria(selectedQuestions, criteriaList);

        List<Question> questions = new ArrayList<>();
        List<MatrixGapDto> gaps = new ArrayList<>();

        for (SelectionCriteria criteria : criteriaList) {
            String key = buildCriteriaKey(criteria);
            List<QuestionBankItem> matchedQuestions = groupedResults.getOrDefault(key, List.of());

            double pointsPerQuestion = criteria.requiredCount > 0 ? criteria.pointsPerCell / criteria.requiredCount : 0;

            for (QuestionBankItem question : matchedQuestions) {
                questions.add(mapToQuestion(question, pointsPerQuestion));
            }

            // Check for gaps
            if (matchedQuestions.size() < criteria.requiredCount) {
                gaps.add(MatrixGapDto.builder()
                        .topic(criteria.topicName)
                        .difficulty(criteria.difficultyStr)
                        .questionType(criteria.questionTypeStr)
                        .requiredCount(criteria.requiredCount)
                        .availableCount(matchedQuestions.size())
                        .build());
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
        double totalPoints = questions.stream().mapToDouble(q -> q.getPoint() != null ? q.getPoint() : 0).sum();

        return AssignmentDraftDto.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(subject)
                .grade(grade)
                .duration(request.getTimeLimitMinutes())
                .questions(questions)
                .missingQuestions(gaps)
                .totalPoints(totalPoints)
                .totalQuestions(questions.size())
                .isComplete(gaps.isEmpty())
                .build();
    }

    /**
     * Build selection criteria from the matrix.
     * Matrix schema: [topic][difficulty][question_type]
     * Questions from any subtopic within a topic count toward the topic's requirements.
     */
    private List<SelectionCriteria> buildSelectionCriteria(AssignmentMatrixDto matrix, MatrixDimensionsDto dimensions) {
        List<SelectionCriteria> criteriaList = new ArrayList<>();

        List<DimensionTopicDto> topics = dimensions.getTopics();
        List<String> difficulties = dimensions.getDifficulties();
        List<String> questionTypes = dimensions.getQuestionTypes();

        log.debug("Matrix dimensions: {} topics, {} difficulties, {} questionTypes",
                topics.size(),
                difficulties.size(),
                questionTypes.size());
        log.debug("Topics: {}", topics.stream().map(DimensionTopicDto::getName).toList());
        log.debug("Difficulties: {}", difficulties);
        log.debug("Question types: {}", questionTypes);

        for (int topicIdx = 0; topicIdx < topics.size(); topicIdx++) {
            DimensionTopicDto topic = topics.get(topicIdx);

            // Collect all subtopic names for this topic
            List<String> subtopicNames = topic.getSubtopics() != null
                    ? topic.getSubtopics().stream().map(DimensionSubtopicDto::getName).toList()
                    : List.of();

            List<List<String>> difficultyRows = matrix.getMatrix().get(topicIdx);

            if (difficultyRows.size() != difficulties.size()) {
                log.warn("Matrix dimension mismatch for topic '{}': expected {} difficulty rows, found {}",
                        topic.getName(),
                        difficulties.size(),
                        difficultyRows.size());
            }

            for (int diffIdx = 0; diffIdx < difficulties.size(); diffIdx++) {
                String difficulty = difficulties.get(diffIdx);
                List<String> questionTypeCells = difficultyRows.get(diffIdx);

                if (questionTypeCells.size() != questionTypes.size()) {
                    log.warn(
                            "Matrix dimension mismatch for topic '{}', difficulty '{}': expected {} question type cells, found {}",
                            topic.getName(),
                            difficulty,
                            questionTypes.size(),
                            questionTypeCells.size());
                }

                for (int qtypeIdx = 0; qtypeIdx < questionTypes.size(); qtypeIdx++) {
                    String cell = questionTypeCells.get(qtypeIdx);
                    String questionType = questionTypes.get(qtypeIdx);

                    // cell format: "count:points"
                    int count = 0;
                    double points = 0.0;
                    if (cell != null && cell.contains(":")) {
                        String[] parts = cell.split(":");
                        count = Integer.parseInt(parts[0]);
                        points = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;
                    }

                    if (count > 0) {
                        Difficulty mappedDifficulty = Difficulty.valueOf(difficulty.toUpperCase());
                        QuestionType mappedType = parseQuestionType(questionType);

                        if (mappedType == null) {
                            log.warn("Skipping cell due to invalid question type '{}' for topic='{}', difficulty='{}'",
                                    questionType,
                                    topic.getName(),
                                    difficulty);
                            continue;
                        }

                        // Store criteria with parent topic and all its subtopics for flexible matching
                        criteriaList.add(new SelectionCriteria(topic.getName(), subtopicNames, difficulty, questionType,
                                mappedDifficulty, mappedType, count, points));
                    } else {
                        log.debug("Skipping cell with count=0 for topic='{}', difficulty='{}', type='{}'",
                                topic.getName(),
                                difficulty,
                                questionType);
                    }
                }
            }
        }

        return criteriaList;
    }

    /**
     * Execute optimized query using window functions.
     * This reduces N queries to 1 query using ROW_NUMBER() OVER (PARTITION BY ...).
     * Filters by subject, grade (if provided), topic, difficulty, and question type.
     */
    private List<QuestionBankItem> selectQuestionsWithWindowFunction(String subject,
            String grade,
            List<SelectionCriteria> criteriaList,
            String teacherId,
            boolean includePersonal) {

        // Build the WHERE clause conditions dynamically
        StringBuilder whereConditions = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("subject", subject);

        // Add grade filter if provided
        if (grade != null && !grade.isEmpty()) {
            parameters.put("grade", grade);
        }

        // First, run a diagnostic query to see what's available in the database
        String diagnosticSql = "SELECT COUNT(*) as cnt, chapter, difficulty, type, grade, subject "
                + "FROM questions WHERE subject = :subject "
                + (grade != null && !grade.isEmpty() ? "AND grade = :grade " : "")
                + "GROUP BY chapter, difficulty, type, grade, subject";

        Query diagnosticQuery = entityManager.createNativeQuery(diagnosticSql);
        diagnosticQuery.setParameter("subject", subject);
        if (grade != null && !grade.isEmpty()) {
            diagnosticQuery.setParameter("grade", grade);
        }

        @SuppressWarnings("unchecked") List<Object[]> availableQuestions = diagnosticQuery.getResultList();
        log.info("=== Available questions in database for subject='{}', grade='{}' ===", subject, grade);
        for (Object[] row : availableQuestions) {
            log.info("  {} questions: chapter='{}', difficulty='{}', type='{}', grade='{}', subject='{}'",
                    row[0],
                    row[1],
                    row[2],
                    row[3],
                    row[4],
                    row[5]);
        }
        log.info("=== End of available questions ===");

        for (int i = 0; i < criteriaList.size(); i++) {
            SelectionCriteria criteria = criteriaList.get(i);
            if (i > 0) {
                whereConditions.append(" OR ");
            }

            String difficultyParam = "difficulty_" + i;
            String typeParam = "type_" + i;
            String countParam = "count_" + i;

            // Build IN clause for subtopics (questions can match any subtopic in the topic)
            StringBuilder subtopicCondition = new StringBuilder("(");
            if (criteria.subtopicNames != null && !criteria.subtopicNames.isEmpty()) {
                subtopicCondition.append("LOWER(chapter) IN (");
                for (int j = 0; j < criteria.subtopicNames.size(); j++) {
                    String subtopicParam = "subtopic_" + i + "_" + j;
                    if (j > 0)
                        subtopicCondition.append(", ");
                    subtopicCondition.append("LOWER(:").append(subtopicParam).append(")");
                    parameters.put(subtopicParam, criteria.subtopicNames.get(j));
                }
                subtopicCondition.append(")");
            } else {
                // Fallback to topic name if no subtopics
                String topicParam = "topic_" + i;
                subtopicCondition.append("LOWER(chapter) = LOWER(:").append(topicParam).append(")");
                parameters.put(topicParam, criteria.topicName);
            }

            whereConditions.append(subtopicCondition)
                    .append(" AND UPPER(difficulty) = UPPER(:")
                    .append(difficultyParam)
                    .append(") AND UPPER(type) = UPPER(:")
                    .append(typeParam)
                    .append(") AND rnk <= :")
                    .append(countParam)
                    .append(")");

            parameters.put(difficultyParam, criteria.difficulty.name());
            parameters.put(typeParam, criteria.questionType.name());
            parameters.put(countParam, criteria.requiredCount);
        }

        // Build the complete query
        String sql = """
                WITH RankedQuestions AS (
                    SELECT q.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY q.chapter, q.difficulty, q.type
                               ORDER BY RANDOM()
                           ) as rnk
                    FROM questions q
                    WHERE q.subject = :subject
                """ + (grade != null && !grade.isEmpty() ? " AND q.grade = :grade" : "")
                + (includePersonal && teacherId != null
                        ? " AND (q.owner_id IS NULL OR q.owner_id = :teacherId)"
                        : " AND q.owner_id IS NULL")
                + """
                        )
                        SELECT id, chapter, context_id, created_at, data, difficulty,
                               explanation, grade, owner_id, subject, title, title_image_url,
                               type, updated_at
                        FROM RankedQuestions
                        WHERE """ + whereConditions.toString();

        log.info("Executing optimized query with {} criteria groups, subject='{}', grade='{}'",
                criteriaList.size(),
                subject,
                grade);

        Query query = entityManager.createNativeQuery(sql, QuestionBankItem.class);

        // Set parameters
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            log.info("  Parameter: {} = {}", entry.getKey(), entry.getValue());
        }

        if (includePersonal && teacherId != null) {
            query.setParameter("teacherId", teacherId.toString());
            log.info("  Parameter: teacherId = {}", teacherId.toString());
        }

        @SuppressWarnings("unchecked") List<QuestionBankItem> results = query.getResultList();

        log.info("Selected {} questions in single query for {} criteria groups", results.size(), criteriaList.size());

        if (results.isEmpty()) {
            log.warn(
                    "No questions found! Check if database has questions matching: subject={}, topics={}, difficulties={}, types={}",
                    subject,
                    criteriaList.stream().map(c -> c.topicName).distinct().toList(),
                    criteriaList.stream().map(c -> c.difficulty).distinct().toList(),
                    criteriaList.stream().map(c -> c.questionType).distinct().toList());
        }

        return results;
    }

    /**
     * Group results by criteria for mapping.
     */
    private Map<String, List<QuestionBankItem>> groupResultsByCriteria(List<QuestionBankItem> questions,
            List<SelectionCriteria> criteriaList) {

        Map<String, List<QuestionBankItem>> grouped = new HashMap<>();

        for (QuestionBankItem question : questions) {
            for (SelectionCriteria criteria : criteriaList) {
                if (matchesCriteria(question, criteria)) {
                    String key = buildCriteriaKey(criteria);
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(question);
                    break; // Each question belongs to only one criteria group
                }
            }
        }

        return grouped;
    }

    /**
     * Check if a question matches the criteria.
     * Now checks if question chapter matches ANY subtopic within the topic (case-insensitive).
     */
    private boolean matchesCriteria(QuestionBankItem question, SelectionCriteria criteria) {
        if (question.getChapter() == null)
            return false;

        boolean chapterMatches = false;
        if (criteria.subtopicNames != null && !criteria.subtopicNames.isEmpty()) {
            // Check if question chapter matches any subtopic
            chapterMatches = criteria.subtopicNames.stream()
                    .anyMatch(subtopic -> question.getChapter().equalsIgnoreCase(subtopic));
        } else {
            // Fallback to topic name matching
            chapterMatches = question.getChapter().equalsIgnoreCase(criteria.topicName);
        }

        return chapterMatches && question.getDifficulty() == criteria.difficulty
                && question.getType() == criteria.questionType;
    }

    /**
     * Build a unique key for criteria grouping.
     */
    private String buildCriteriaKey(SelectionCriteria criteria) {
        return criteria.topicName + "|" + criteria.difficulty + "|" + criteria.questionType;
    }

    /**
     * Map a QuestionBankItem entity to Question entity for Assignment.
     */
    private Question mapToQuestion(QuestionBankItem question, double points) {
        return Question.builder()
                .id(question.getId())
                .type(question.getType())
                .difficulty(question.getDifficulty())
                .title(question.getTitle())
                .titleImageUrl(question.getTitleImageUrl())
                .explanation(question.getExplanation())
                .grade(question.getGrade())
                .chapter(question.getChapter())
                .subject(question.getSubject())
                .contextId(question.getContextId())
                .data(question.getData())
                .point(points)
                .build();
    }

    /**
     * Parse question type string to QuestionType enum.
     * Accepts uppercase, lowercase, and underscore/hyphen variations.
     * Examples: "multiple_choice", "MULTIPLE_CHOICE", "multiple-choice" all map to MULTIPLE_CHOICE
     */
    private QuestionType parseQuestionType(String questionType) {
        if (questionType == null)
            return null;
        try {
            // Normalize: uppercase and replace hyphens with underscores
            String normalized = questionType.toUpperCase().replace('-', '_');
            return QuestionType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown question type: '{}'. Valid types: {}",
                    questionType,
                    Arrays.toString(QuestionType.values()));
            return null;
        }
    }

    /**
     * Internal record to hold selection criteria.
     * Now includes all subtopic names to allow matching questions from any subtopic within a topic.
     */
    private record SelectionCriteria(String topicName, List<String> subtopicNames, String difficultyStr,
            String questionTypeStr, Difficulty difficulty, QuestionType questionType, int requiredCount,
            double pointsPerCell) {
    }
}
