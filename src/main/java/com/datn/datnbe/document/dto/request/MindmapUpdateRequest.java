package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.MindmapNodeDto;
import com.datn.datnbe.document.dto.MindmapEdgeDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.Size;
import lombok.*;

import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MindmapUpdateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private List<MindmapNodeDto> nodes;

    private List<MindmapEdgeDto> edges;

    @Builder.Default
    Map<String, Object> extraFields = new java.util.HashMap<>();

    @JsonAnySetter
    public void setExtraField(String key, Object value) {
        extraFields.put(key, value);
    }

    @JsonAnyGetter
    public java.util.Map<String, Object> getExtraFields() {
        return extraFields;
    }

}
