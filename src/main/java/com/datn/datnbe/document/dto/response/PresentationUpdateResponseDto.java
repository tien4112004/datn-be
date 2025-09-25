package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private List<SlideDto> slides;
    private Map<String, Object> metaData = new java.util.HashMap<>();

    @JsonAnySetter
    public void setMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetaData() {
        return metaData;
    }
}
