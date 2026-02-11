package com.datn.datnbe.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * AI Gateway DTO for Open-Ended question data.
 * Represents the raw data structure received from AI Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenEndedData {

    @JsonProperty("expectedAnswer")
    @JsonAlias({"expected_answer", "expectedAnswer"})
    String expectedAnswer;

    @JsonProperty("maxLength")
    @JsonAlias({"max_length", "maxLength"})
    Integer maxLength;
}
