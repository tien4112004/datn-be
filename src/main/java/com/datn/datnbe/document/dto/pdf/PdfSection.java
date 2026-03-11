package com.datn.datnbe.document.dto.pdf;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * A renderable section in the PDF — either a context block with its linked
 * questions, or a group of standalone questions. Sections are ordered by first
 * occurrence in the original questions list, matching the frontend Question List tab.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfSection {

    /** "context" or "standalone" — mirrors the frontend QuestionGroup.type */
    String type;

    /** Non-null when type == "context" */
    PdfContextBlock contextBlock;

    /** Questions in this section (context-linked or standalone) */
    List<PdfQuestionViewModel> questions;
}
