package com.datn.datnbe.cms.entity.answerData;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedAnswer {
    String response;
    String responseUrl;
}
