package com.datn.datnbe.document.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Domain entity for Multiple Choice option.
 * Used for storage and frontend responses.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceOption {
    String id;
    String text;
    String imageUrl;
    Boolean isCorrect;
}
