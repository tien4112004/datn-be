package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationUpdateResponseDto {
    private String id;
    private String title;
    private String thumbnail;
    private List<SlideDto> slides;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
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
