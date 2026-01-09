package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "MULTIPLE_CHOICE|MATCHING|FILL_IN_THE_BLANK|OPEN_ENDED", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid question type")
    String type;

    @NotBlank(message = "Difficulty is required")
    @Pattern(regexp = "KNOWLEDGE|COMPREHENSION|APPLICATION|ADVANCED_APPLICATION", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid difficulty level")
    String difficulty;

    String explanation;

    String titleImageUrl;

    Integer points;

    Object data;
}
