package com.datn.datnbe.cms.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlankSegment {

    String id;
    SegmentType type;
    String content;
    List<String> acceptableAnswers;

    public enum SegmentType {
        TEXT,
        BLANK
    }
}
