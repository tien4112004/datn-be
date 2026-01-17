package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.questiondata.Difficulty;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.OpenEndedData;
import com.datn.datnbe.document.entity.questiondata.QuestionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {

    String id;
    QuestionType type;
    Difficulty difficulty;
    String title;
    String titleImageUrl;
    String explanation;
    String grade;
    String chapter;
    String subject;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = OpenEndedData.class, name = "OPEN_ENDED"),
            @JsonSubTypes.Type(value = MultipleChoiceData.class, name = "MULTIPLE_CHOICE"),
            @JsonSubTypes.Type(value = MatchingData.class, name = "MATCHING"),
            @JsonSubTypes.Type(value = FillInBlankData.class, name = "FILL_IN_BLANK")})
    Object data;

    // Additional fields for assignment specific data
    Double point;
}
