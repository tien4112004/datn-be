package com.datn.datnbe.document.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.HashMap;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlideDto {
    String id;

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
