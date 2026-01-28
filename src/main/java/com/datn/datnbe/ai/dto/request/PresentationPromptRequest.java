package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class PresentationPromptRequest {

    @NotBlank(message = "Outline cannot be blank")
    @Size(min = 1, message = "Outline must have at least 1 character")
    String outline;
    String model;
    String language;

    @JsonAlias("slide_count, slideCount")
    Integer slideCount;
    String provider;
    String presentationId;
    String topic;
    GenerationOptionsDto generationOptions;

    @Size(max = 50, message = "Grade must not exceed 50 characters")
    String grade;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    String subject;
}
