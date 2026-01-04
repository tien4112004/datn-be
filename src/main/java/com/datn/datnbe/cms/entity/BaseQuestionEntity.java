package com.datn.datnbe.cms.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseQuestionEntity {

    String id;
    QuestionType type;
    Difficulty difficulty;
    String title;
    String titleImageUrl;
    String explanation;
    Integer points;
    QuestionData data;

    public abstract QuestionType getType();

    public enum QuestionType {
        MULTIPLE_CHOICE,
        MATCHING,
        OPEN_ENDED,
        FILL_IN_BLANK
    }

    public enum Difficulty {
        KNOWLEDGE,
        COMPREHENSION,
        APPLICATION,
        ADVANCED_APPLICATION
    }

}
