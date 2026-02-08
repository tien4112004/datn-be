package com.datn.datnbe.cms.entity.answerData;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerData {
    String id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = OpenEndedAnswer.class, name = "OPEN_ENDED"),
            @JsonSubTypes.Type(value = MultipleChoiceAnswer.class, name = "MULTIPLE_CHOICE"),
            @JsonSubTypes.Type(value = MatchingAnswer.class, name = "MATCHING"),
            @JsonSubTypes.Type(value = FillInBlankAnswer.class, name = "FILL_IN_BLANK")})
    Object answer;

    Double point;

    String feedback;

    boolean isAutoGraded;
}
