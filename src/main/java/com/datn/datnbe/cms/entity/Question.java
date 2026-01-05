package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.entity.questiondata.Difficulty;
import com.datn.datnbe.cms.entity.questiondata.QuestionType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonDeserialize(using = QuestionDeserializer.class)
public class Question {

    String id;
    QuestionType type;
    Difficulty difficulty;
    String title;
    String titleImageUrl;
    String explanation;
    Integer points;
    QuestionData data;
}
