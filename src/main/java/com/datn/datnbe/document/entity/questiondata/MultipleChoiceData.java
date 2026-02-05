package com.datn.datnbe.document.entity.questiondata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

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
