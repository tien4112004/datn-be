package com.datn.datnbe.document.dto.pdf;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Top-level view-model passed into the Thymeleaf PDF template.
 * All data has been pre-processed and type-cast by AssignmentPdfViewModelMapper.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentPdfViewModel {

    String title;
    String subject;
    String grade;
    String chapter;
    String description;
    double totalPoints;

    /**
     * Context passages with their linked questions, ordered by first appearance
     * of the contextId in the original questions list.
     */
    List<PdfContextBlock> contextBlocks;

    /**
     * Questions whose contextId is null or references an unknown context,
     * in their original list order.
     */
    List<PdfQuestionViewModel> standaloneQuestions;
}
