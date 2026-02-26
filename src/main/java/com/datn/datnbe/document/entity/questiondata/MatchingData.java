package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

/**
 * Domain entity for Matching question data.
 * Used for storage and frontend responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchingData {

    List<MatchingPair> pairs;

    @Builder.Default
    @JsonProperty("shufflePairs")
    @JsonAlias({"shuffle_pairs", "shufflePairs"})
    Boolean shufflePairs = false;
}
