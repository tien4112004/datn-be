package com.datn.datnbe.cms.entity.questiondata;

import com.datn.datnbe.cms.entity.QuestionData;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingData implements QuestionData {

    List<MatchingPair> pairs;

    @Builder.Default
    Boolean shufflePairs = false;
}
