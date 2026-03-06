package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;
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

    String grade;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    String subject;

    @Size(max = 255, message = "Chapter must not exceed 255 characters")
    String chapter;

    @JsonProperty("file_urls")
    @JsonAlias("fileUrls")
    List<String> fileUrls;
}
