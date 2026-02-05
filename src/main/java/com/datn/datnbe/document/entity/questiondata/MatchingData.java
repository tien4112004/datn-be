package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingData {

    List<MatchingPair> pairs;

    @Builder.Default
    @JsonProperty("shufflePairs")
    @JsonAlias({"shuffle_pairs", "shufflePairs"})
    Boolean shufflePairs = false;
}
