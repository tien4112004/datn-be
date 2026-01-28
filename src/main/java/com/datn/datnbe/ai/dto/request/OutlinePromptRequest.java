package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class OutlinePromptRequest {
    String topic;
    String language;
    String model;
    @JsonAlias("slide_count, slideCount")
    int slideCount;
    String provider;

    @Size(max = 50, message = "Grade must not exceed 50 characters")
    String grade;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    String subject;
}
