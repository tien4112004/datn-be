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

    String grade;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    String subject;

    // ✅ ADD: Full presentation config with theme data from frontend
    PresentationConfigDto presentation;
}
