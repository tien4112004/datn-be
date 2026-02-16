package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.document.entity.questiondata.BlankSegment;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

/**
 * Fill-in-blank data format from AI Gateway.
 * AI generates text with {{answer|alternative}} placeholders that need to be parsed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FillInBlankData {

    /**
     * Raw text with {{answer|alternative}} placeholders from AI.
     * Example: "The capital of Vietnam is {{Hà Nội|Hanoi}}."
     */
    @JsonProperty("data")
    String data;

    @JsonProperty("segments")
    List<BlankSegment> segments;

    /**
     * Whether answer checking should be case-sensitive.
     */
    @Builder.Default
    @JsonProperty("caseSensitive")
    @JsonAlias({"case_sensitive", "caseSensitive"})
    Boolean caseSensitive = false;
}
