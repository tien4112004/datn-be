package com.datn.datnbe.document.exam.dto.response;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a question selected for an exam.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDto {

    /**
     * Unique identifier for the question.
     */
    private UUID questionId;

    /**
     * Question content/text.
     */
    private String content;

    /**
     * Type of question.
     */
    private QuestionType questionType;

    /**
     * Topic the question belongs to.
     */
    private String topic;

    /**
     * Difficulty level.
     */
    private Difficulty difficulty;

    /**
     * Points allocated for this question in the exam.
     */
    private Double points;

    /**
     * Order/position in the exam.
     */
    private Integer orderIndex;

    /**
     * Answer options (for multiple choice, matching, etc.).
     */
    private Object answers;

    /**
     * Correct answer(s).
     */
    private Object correctAnswer;

    /**
     * Explanation for the answer.
     */
    private String explanation;
}
