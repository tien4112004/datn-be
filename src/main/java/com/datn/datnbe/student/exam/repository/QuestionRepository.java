package com.datn.datnbe.student.exam.repository;

import com.datn.datnbe.student.exam.entity.Question;
import com.datn.datnbe.student.exam.enums.ExamDifficulty;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import com.datn.datnbe.student.exam.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT q FROM Question q WHERE q.ownerId = :ownerId")
    Page<Question> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.contextId = :contextId ORDER BY q.questionNumber")
    List<Question> findByContextIdOrderByQuestionNumber(@Param("contextId") UUID contextId);

    @Query("SELECT q FROM Question q WHERE q.ownerId = :ownerId "
            + "AND (:questionType IS NULL OR q.questionType = :questionType) "
            + "AND (:topic IS NULL OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) "
            + "AND (:gradeLevel IS NULL OR q.gradeLevel = :gradeLevel) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<Question> findByOwnerIdWithFilters(@Param("ownerId") UUID ownerId,
            @Param("questionType") QuestionType questionType,
            @Param("topic") String topic,
            @Param("gradeLevel") GradeLevel gradeLevel,
            @Param("difficulty") ExamDifficulty difficulty,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.questionId IN :questionIds")
    List<Question> findByQuestionIdIn(@Param("questionIds") List<UUID> questionIds);

    @Query("SELECT q FROM Question q WHERE q.questionId = :questionId AND q.ownerId = :ownerId")
    Optional<Question> findByQuestionIdAndOwnerId(@Param("questionId") UUID questionId, @Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.contextId = :contextId")
    Long countByContextId(@Param("contextId") UUID contextId);

    /**
     * Find questions matching specific criteria for matrix-based selection.
     * Includes both personal questions (owned by the user) and public questions (ownerId is null).
     */
    @Query("SELECT q FROM Question q WHERE " + "(q.ownerId = :ownerId OR q.ownerId IS NULL) "
            + "AND (:topic IS NULL OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
            + "AND (:questionType IS NULL OR q.questionType = :questionType) " + "ORDER BY FUNCTION('RANDOM')")
    List<Question> findMatchingQuestionsForMatrix(@Param("ownerId") UUID ownerId,
            @Param("topic") String topic,
            @Param("difficulty") ExamDifficulty difficulty,
            @Param("questionType") QuestionType questionType,
            Pageable pageable);

    /**
     * Find only public questions (no owner) matching specific criteria.
     */
    @Query("SELECT q FROM Question q WHERE " + "q.ownerId IS NULL "
            + "AND (:topic IS NULL OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
            + "AND (:questionType IS NULL OR q.questionType = :questionType) " + "ORDER BY FUNCTION('RANDOM')")
    List<Question> findPublicMatchingQuestions(@Param("topic") String topic,
            @Param("difficulty") ExamDifficulty difficulty,
            @Param("questionType") QuestionType questionType,
            Pageable pageable);

    /**
     * Count questions matching specific criteria for availability check.
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE " + "(q.ownerId = :ownerId OR q.ownerId IS NULL) "
            + "AND (:topic IS NULL OR LOWER(q.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
            + "AND (:questionType IS NULL OR q.questionType = :questionType)")
    Long countMatchingQuestions(@Param("ownerId") UUID ownerId,
            @Param("topic") String topic,
            @Param("difficulty") ExamDifficulty difficulty,
            @Param("questionType") QuestionType questionType);
}
