package com.datn.datnbe.document.dto.pdf;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * A reading-passage block with its linked questions.
 * Rendered as a boxed passage followed immediately by the questions that reference it.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfContextBlock {

    String id;
    String title;
    String content;
    String author;

    List<PdfQuestionViewModel> questions;
}
