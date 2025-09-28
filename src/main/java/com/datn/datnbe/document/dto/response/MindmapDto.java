package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.MindmapNodeDto;
import com.datn.datnbe.document.dto.MindmapEdgeDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MindmapDto {
    private String id;
    private String title;
    private String description;
    //    private String ownerId;

    @Valid
    private List<MindmapNodeDto> nodes;

    @Valid
    private List<MindmapEdgeDto> edges;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
