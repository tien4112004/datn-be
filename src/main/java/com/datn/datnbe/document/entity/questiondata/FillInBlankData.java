package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

/**
 * Fill-in-blank data for backend storage and frontend responses.
 * Contains parsed segments structure.
 *
 * Note: AI Gateway sends raw text format (see FillInBlankAIData),
 * which is converted to this structure by FillInBlankParser.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FillInBlankData {

    @JsonProperty("data")
    String data;

    /**
     * Parsed segments (TEXT and BLANK) for storage and frontend.
     */
    List<BlankSegment> segments;

    /**
     * Whether answer checking should be case-sensitive.
     */
    @Builder.Default
    @JsonProperty("caseSensitive")
    @JsonAlias({"case_sensitive", "caseSensitive"})
    Boolean caseSensitive = false;
}
