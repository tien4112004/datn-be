package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
