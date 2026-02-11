package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

/**
 * AI Gateway DTO for Multiple Choice question data.
 * Represents the raw data structure received from AI Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceData {

    List<MultipleChoiceOption> options;

    @Builder.Default
    @JsonProperty("shuffleOptions")
    @JsonAlias({"shuffle_options", "shuffleOptions"})
    Boolean shuffleOptions = false;
}
