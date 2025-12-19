package com.datn.datnbe.document.dto.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtStyleCreateRequest {

    @NotBlank(message = "Art style ID is required")
    @Size(max = 50, message = "Art style ID must not exceed 50 characters")
    private String id;

    @NotBlank(message = "Art style name is required")
    @Size(max = 255, message = "Art style name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Art style label key is required")
    @Size(max = 100, message = "Art style label key must not exceed 100 characters")
    private String labelKey;

    @Size(max = 500, message = "Art style visual must not exceed 500 characters")
    private String visual;

    @Size(max = 1000, message = "Art style modifiers must not exceed 1000 characters")
    private String modifiers;

    @Builder.Default
    private Boolean isEnabled = true;

    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }
}
