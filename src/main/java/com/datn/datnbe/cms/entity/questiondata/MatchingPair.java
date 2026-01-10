package com.datn.datnbe.cms.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingPair {

    @Builder.Default
    String id = UUID.randomUUID().toString();
    String left;
    String leftImageUrl;
    String right;
    String rightImageUrl;
}
