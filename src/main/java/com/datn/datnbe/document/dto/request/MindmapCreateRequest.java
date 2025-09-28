package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.MindmapNodeDto;
import com.datn.datnbe.document.dto.MindmapEdgeDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MindmapCreateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description;

    @Valid
    List<MindmapNodeDto> nodes;

    @Valid
    List<MindmapEdgeDto> edges;

    @Builder.Default
    Map<String, Object> extraFields = new HashMap<>();

    @JsonAnySetter
    public void setExtraField(String key, Object value) {
        extraFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return extraFields;
    }
}
