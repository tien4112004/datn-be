package com.datn.datnbe.cms.entity.answerData;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Answer data for Multiple Choice questions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceAnswer {
    String id;
}
