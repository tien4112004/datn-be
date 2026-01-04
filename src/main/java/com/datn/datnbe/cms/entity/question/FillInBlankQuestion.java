package com.datn.datnbe.cms.entity.question;

import com.datn.datnbe.cms.entity.BaseQuestionEntity;
import com.datn.datnbe.cms.entity.questiondata.FillInBlankData;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FillInBlankQuestion extends BaseQuestionEntity {

    FillInBlankData fillInBlankData;

    @Override
    public QuestionType getType() {
        return QuestionType.FILL_IN_BLANK;
    }
}
