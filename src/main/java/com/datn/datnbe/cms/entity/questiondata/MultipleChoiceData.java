package com.datn.datnbe.cms.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceData {

    List<MultipleChoiceOption> options;

    @Builder.Default
    Boolean shuffleOptions = false;
}
