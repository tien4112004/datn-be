package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Fill-in-blank data format from AI Gateway.
 * AI generates text with {{answer|alternative}} placeholders that need to be parsed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FillInBlankData {

    /**
     * Raw text with {{answer|alternative}} placeholders from AI.
     * Example: "The capital of Vietnam is {{Hà Nội|Hanoi}}."
     */
    @JsonProperty("data")
    String data;

    /**
     * Whether answer checking should be case-sensitive.
     */
    @Builder.Default
    @JsonProperty("caseSensitive")
    @JsonAlias({"case_sensitive", "caseSensitive"})
    Boolean caseSensitive = false;
}
