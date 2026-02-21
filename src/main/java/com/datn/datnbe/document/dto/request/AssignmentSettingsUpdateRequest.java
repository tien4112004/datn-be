package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.entity.AssessmentMatrixCell;
import com.datn.datnbe.document.entity.AssignmentTopic;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentSettingsUpdateRequest {
    Integer maxSubmissions;
    Boolean allowRetake;
    Boolean showCorrectAnswers;
    Boolean showScoreImmediately;
    Double passingScore;
    Date availableFrom;
    Date availableUntil;
    List<AssignmentTopic> topics;
    List<AssessmentMatrixCell> matrixCells;
}
