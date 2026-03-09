package com.datn.datnbe.document.dto.pdf;

import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * View-model for a single question in the PDF.
 * All type casting happens in AssignmentPdfViewModelMapper — exactly one data field is non-null.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfQuestionViewModel {

    /** Sequential display number across the whole assignment (1, 2, 3…). */
    int number;

    String title;
    QuestionType type;
    Double point;

    MultipleChoiceData multipleChoiceData;
    FillInBlankData fillInBlankData;
    OpenEndedData openEndedData;
    MatchingData matchingData;
}
