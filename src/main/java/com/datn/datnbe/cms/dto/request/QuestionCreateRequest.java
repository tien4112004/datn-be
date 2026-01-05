package com.datn.datnbe.cms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCreateRequest {

    @NotBlank(message = "Title is required")
    String title;

    @NotNull(message = "Question type is required")
    String type;

    String difficulty;

    String explanation;

    String titleImageUrl;

    Integer points;

    @Valid
    Object data;
}
