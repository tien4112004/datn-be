package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("acceptableAnswers")
    @JsonAlias({"acceptable_answers", "acceptableAnswers"})
    List<String> acceptableAnswers;

    public enum SegmentType {
        TEXT, BLANK
    }
}
