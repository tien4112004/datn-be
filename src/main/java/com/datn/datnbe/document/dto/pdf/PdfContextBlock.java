package com.datn.datnbe.document.dto.pdf;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * A reading-passage block rendered as a boxed passage header.
 * Questions linked to this context are stored in the parent PdfSection.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfContextBlock {

    String id;
    String title;
    String content;
    String author;
}
