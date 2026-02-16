package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

/**
 * AI Gateway DTO for Matching question data.
 * Represents the raw data structure received from AI Gateway.
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
