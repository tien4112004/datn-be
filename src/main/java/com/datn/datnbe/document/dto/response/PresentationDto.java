package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
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
    private Map<String, Object> metaData = new java.util.HashMap<>();
    private List<SlideDto> slides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isParsed;

    @JsonAnySetter
    public void setMetaData(String key, Object value) {
        metaData.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMetaData() {
        return metaData;
    }
}
