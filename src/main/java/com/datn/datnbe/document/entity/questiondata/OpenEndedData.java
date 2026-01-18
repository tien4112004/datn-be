package com.datn.datnbe.document.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedData {

    String expectedAnswer;
    Integer maxLength;
}
