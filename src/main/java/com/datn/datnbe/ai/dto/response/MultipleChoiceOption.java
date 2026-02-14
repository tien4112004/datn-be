package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * AI Gateway DTO for Multiple Choice option.
 * Represents a single option received from AI Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleChoiceOption {

    String text;

    @JsonProperty("imageUrl")
    @JsonAlias({"image_url", "imageUrl"})
    String imageUrl;

    @JsonProperty("isCorrect")
    @JsonAlias({"is_correct", "correct"})
    Boolean isCorrect;
}
