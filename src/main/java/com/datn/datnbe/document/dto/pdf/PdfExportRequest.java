package com.datn.datnbe.document.dto.pdf;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfExportRequest {

    /** Visual theme. Defaults to CLASSIC (Liberation Serif / Times New Roman look). */
    PdfStyleTheme theme = PdfStyleTheme.CLASSIC;

    /** Header customization — school name, chapter, description toggles. */
    PdfHeaderConfig headerConfig = new PdfHeaderConfig();

    /** Show the point value next to each question number. Default true. */
    Boolean showQuestionPoints = true;

    /** Append an answer key section at the end of the PDF. Default false. */
    Boolean showAnswerKey = false;

    /** Show the explanation text below each question. Default false. */
    Boolean showExplanations = false;

    /**
     * Reserved for future use: overlay a student's submitted answers onto the PDF.
     * Pass a submission ID to enable. Currently has no effect.
     */
    String submissionId;
}
