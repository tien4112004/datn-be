package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
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
