package com.datn.datnbe.document.repository;

import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<QuestionBankItem, String> {

    Page<QuestionBankItem> findBySubject(String subject, Pageable pageable);

    List<QuestionBankItem> findByContextId(String contextId);

    @Query("SELECT q FROM QuestionBankItem q WHERE q.subject = :subject "
            + "AND (:questionType IS NULL OR q.type = :questionType) "
            + "AND (:chapter IS NULL OR LOWER(q.chapter) LIKE LOWER(CONCAT('%', :chapter, '%'))) "
            + "AND (:grade IS NULL OR q.grade = :grade) " + "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<QuestionBankItem> findBySubjectWithFilters(@Param("subject") String subject,
            @Param("questionType") QuestionType questionType,
            @Param("chapter") String chapter,
            @Param("grade") String grade,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);

    List<QuestionBankItem> findByIdIn(List<String> questionIds);

    Optional<QuestionBankItem> findByIdAndOwnerId(String questionId, String ownerId);

    Long countByContextId(String contextId);

    /**
     * Find questions matching specific criteria for matrix-based selection.
     */
    @Query("SELECT q FROM QuestionBankItem q WHERE "
            + "(:ownerId IS NULL OR q.ownerId = :ownerId OR q.ownerId IS NULL) " + "AND q.subject = :subject "
            + "AND (:chapter IS NULL OR LOWER(q.chapter) LIKE LOWER(CONCAT('%', :chapter, '%'))) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
            + "AND (:questionType IS NULL OR q.type = :questionType) " + "ORDER BY FUNCTION('RANDOM')")
    List<QuestionBankItem> findMatchingQuestionsForMatrix(@Param("ownerId") String ownerId,
            @Param("subject") String subject,
            @Param("chapter") String chapter,
            @Param("difficulty") Difficulty difficulty,
            @Param("questionType") QuestionType questionType,
            Pageable pageable);

    /**
     * Count questions matching specific criteria for availability check.
     */
    @Query("SELECT COUNT(q) FROM QuestionBankItem q WHERE "
            + "(:ownerId IS NULL OR q.ownerId = :ownerId OR q.ownerId IS NULL) " + "AND q.subject = :subject "
            + "AND (:chapter IS NULL OR LOWER(q.chapter) LIKE LOWER(CONCAT('%', :chapter, '%'))) "
            + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
            + "AND (:questionType IS NULL OR q.type = :questionType)")
    Long countMatchingQuestions(@Param("ownerId") String ownerId,
            @Param("subject") String subject,
            @Param("chapter") String chapter,
            @Param("difficulty") Difficulty difficulty,
            @Param("questionType") QuestionType questionType);
}
