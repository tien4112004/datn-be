package com.datn.datnbe.document.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Domain entity for Matching pair.
 * Used for storage and frontend responses.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingPair {
    String id;
    String left;
    String leftImageUrl;
    String right;
    String rightImageUrl;
}
