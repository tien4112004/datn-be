package com.datn.datnbe.document.dto.request;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionUpdateRequest {

    String title;

    String type;

    String difficulty;

    String explanation;

    String titleImageUrl;

    String grade;

    String chapter;

    @Valid
    Object data;
}
