package com.datn.datnbe.cms.entity.questiondata;

import com.datn.datnbe.cms.entity.QuestionData;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedData implements QuestionData {

    String expectedAnswer;
    Integer maxLength;
}
