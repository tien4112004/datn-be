package com.datn.datnbe.cms.dto;

import com.datn.datnbe.cms.entity.answerData.FillInBlankAnswer;
import com.datn.datnbe.cms.entity.answerData.MatchingAnswer;
import com.datn.datnbe.cms.entity.answerData.MultipleChoiceAnswer;
import com.datn.datnbe.cms.entity.answerData.OpenEndedAnswer;
import com.datn.datnbe.cms.enums.AnswerType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.auto.value.AutoValue.Builder;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDataDto {
    String id;

    @Enumerated(EnumType.STRING)
    AnswerType type;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = OpenEndedAnswer.class, name = "OPEN_ENDED"),
            @JsonSubTypes.Type(value = MultipleChoiceAnswer.class, name = "MULTIPLE_CHOICE"),
            @JsonSubTypes.Type(value = MatchingAnswer.class, name = "MATCHING"),
            @JsonSubTypes.Type(value = FillInBlankAnswer.class, name = "FILL_IN_BLANK")})
    Object answer;
}
