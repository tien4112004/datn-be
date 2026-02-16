package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * AI Gateway DTO for Matching pair.
 * Represents a single matching pair received from AI Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchingPair {

    String left;

    @JsonProperty("leftImageUrl")
    @JsonAlias({"left_image_url", "leftImageUrl"})
    String leftImageUrl;

    String right;

    @JsonProperty("rightImageUrl")
    @JsonAlias({"right_image_url", "rightImageUrl"})
    String rightImageUrl;
}
