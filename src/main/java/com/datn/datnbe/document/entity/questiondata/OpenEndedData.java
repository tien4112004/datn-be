package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
