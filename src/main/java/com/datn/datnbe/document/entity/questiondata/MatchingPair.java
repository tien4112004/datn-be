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
public class MatchingPair {

    @Builder.Default
    String id = UUID.randomUUID().toString();
    String left;

    @JsonProperty("leftImageUrl")
    @JsonAlias({"left_image_url", "leftImageUrl"})
    String leftImageUrl;

    String right;

    @JsonProperty("rightImageUrl")
    @JsonAlias({"right_image_url", "rightImageUrl"})
    String rightImageUrl;
}
