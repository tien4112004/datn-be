package com.datn.datnbe.cms.entity.answerData;

import com.datn.datnbe.cms.enums.AnswerType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedAnswer {
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    AnswerType type;

    String response;
    String responseUrl;
}
