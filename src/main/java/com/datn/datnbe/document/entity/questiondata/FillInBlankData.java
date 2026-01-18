package com.datn.datnbe.document.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FillInBlankData {

    List<BlankSegment> segments;

    @Builder.Default
    Boolean caseSensitive = false;
}
