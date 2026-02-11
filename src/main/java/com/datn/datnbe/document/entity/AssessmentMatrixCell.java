package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssessmentMatrixCell {
    String topicId;
    Difficulty difficulty;
    Integer requiredCount;
    Integer currentCount;  // Calculated
}
