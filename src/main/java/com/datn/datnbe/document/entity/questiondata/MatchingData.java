package com.datn.datnbe.document.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingData {

    List<MatchingPair> pairs;

    @Builder.Default
    Boolean shufflePairs = false;
}
