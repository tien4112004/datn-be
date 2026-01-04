package com.datn.datnbe.cms.entity.question;

import com.datn.datnbe.cms.entity.BaseQuestionEntity;
import com.datn.datnbe.cms.entity.questiondata.OpenEndedData;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedQuestion extends BaseQuestionEntity {

    OpenEndedData openEndedData;

    @Override
    public QuestionType getType() {
        return QuestionType.OPEN_ENDED;
    }
}
