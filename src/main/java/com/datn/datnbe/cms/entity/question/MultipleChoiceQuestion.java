package com.datn.datnbe.cms.entity.question;

import com.datn.datnbe.cms.entity.BaseQuestionEntity;
import com.datn.datnbe.cms.entity.questiondata.MultipleChoiceData;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceQuestion extends BaseQuestionEntity {

    MultipleChoiceData multipleChoiceData;

    @Override
    public QuestionType getType() {
        return QuestionType.MULTIPLE_CHOICE;
    }
}
