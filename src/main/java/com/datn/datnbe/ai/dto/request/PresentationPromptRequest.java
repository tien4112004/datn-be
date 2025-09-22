package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("slide_count")
    Integer slideCount;
    String provider;

    @JsonProperty("meta_data")
    private Object metaData;
}
