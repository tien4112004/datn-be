package com.datn.datnbe.document.exam.enums;

/**
 * Strategy for handling missing questions when generating an exam from a matrix.
 */
public enum MissingQuestionStrategy {
    /**
     * Return the exam draft with gaps indicated.
     * The user can review and fill in the gaps manually.
     */
    REPORT_GAPS,

    /**
     * Automatically generate missing questions using AI.
     */
    GENERATE_WITH_AI,

    /**
     * Fail the request if not all requirements can be met from the question bank.
     */
    FAIL_FAST
}
