package com.datn.datnbe.cms.entity.answerData;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingAnswer {
    Map<String, String> matchedPairs;

    public boolean verifyAnswer(Map<String, String> correctPairs) {
        return this.matchedPairs.equals(correctPairs);
    }
}
