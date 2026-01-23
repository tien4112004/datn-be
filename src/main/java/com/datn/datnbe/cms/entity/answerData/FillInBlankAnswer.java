package com.datn.datnbe.cms.entity.answerData;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FillInBlankAnswer {
    Map<String, String> blankAnswers;
}
