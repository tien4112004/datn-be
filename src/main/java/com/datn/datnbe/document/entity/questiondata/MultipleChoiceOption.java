package com.datn.datnbe.document.entity.questiondata;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Domain entity for Multiple Choice option.
 * Used for storage and frontend responses.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipleChoiceOption {
    @Builder.Default
    String id = UUID.randomUUID().toString();
    String text;
    @JsonProperty("imageUrl")
    @JsonAlias({"image_url", "imageUrl"})
    String imageUrl;
    @JsonProperty("isCorrect")
    @JsonAlias({"is_correct", "correct"})
    Boolean isCorrect;
}
