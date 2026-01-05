package com.datn.datnbe.cms.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
