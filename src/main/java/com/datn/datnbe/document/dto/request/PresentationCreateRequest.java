package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PresentationCreateRequest {
    private String id;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Slides cannot be null")
    @Valid
    private List<SlideDto> slides;
    Boolean isParsed;

    private Map<String, Object> metadata = new java.util.HashMap<>();

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public java.util.Map<String, Object> getMetadata() {
        return metadata;
    }
}
