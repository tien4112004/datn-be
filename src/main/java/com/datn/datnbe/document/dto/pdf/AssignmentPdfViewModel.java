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
     * Ordered sections matching the frontend Question List tab order.
     * Each section is either a context block (with its linked questions)
     * or a standalone question group, in first-occurrence order.
     */
    List<PdfSection> sections;
}
