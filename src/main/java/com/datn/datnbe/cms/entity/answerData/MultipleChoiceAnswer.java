package com.datn.datnbe.cms.entity.answerData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.datn.datnbe.cms.enums.AnswerType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    AnswerType type;

    public boolean verifyAnswer(String selectedOptionId) {
        return this.id.equals(selectedOptionId);
    }
}
