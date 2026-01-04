package com.datn.datnbe.cms.entity.question;

import com.datn.datnbe.cms.entity.BaseQuestionEntity;
import com.datn.datnbe.cms.entity.questiondata.MatchingData;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingQuestion extends BaseQuestionEntity {

    MatchingData matchingData;

    @Override
    public QuestionType getType() {
        return QuestionType.MATCHING;
    }
}
