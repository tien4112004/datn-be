package com.datn.datnbe.cms.entity.questiondata;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlankSegment {

    @Builder.Default
    String id = UUID.randomUUID().toString();
    SegmentType type;
    String content;
    List<String> acceptableAnswers;

    public enum SegmentType {
        TEXT, BLANK
    }
}
