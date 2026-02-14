package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationUpdateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @NotNull(message = "Title cannot be null")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Slides cannot be null")
    @NotEmpty(message = "Presentation must contain at least one slide")
    @Valid
    private List<SlideDto> slides;

    private String thumbnail;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // Capture any unknown JSON properties into metadata
    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
