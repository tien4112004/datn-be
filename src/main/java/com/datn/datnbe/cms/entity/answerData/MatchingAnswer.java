package com.datn.datnbe.cms.entity.answerData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;

import com.datn.datnbe.cms.enums.AnswerType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingAnswer {
    Map<String, String> matchedPairs;
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    AnswerType type;

    public boolean verifyAnswer(Map<String, String> correctPairs) {
        return this.matchedPairs.equals(correctPairs);
    }
}
