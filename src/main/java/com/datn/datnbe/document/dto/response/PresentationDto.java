package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for detailed presentation information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationDto {
    private String id;
    private String title;
    @Builder.Default
    private String thumbnail;
    private Map<String, Object> metadata = new java.util.HashMap<>();
    private List<SlideDto> slides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isParsed;
    @Builder.Default
    private Map<String, Object> addtionalData = new HashMap<>();

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @JsonAnySetter
    public void setAddtionalData(String key, Object value) {
        addtionalData.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAddtionalData() {
        return addtionalData;
    }
}
