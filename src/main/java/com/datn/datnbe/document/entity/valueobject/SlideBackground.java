package com.datn.datnbe.document.entity.valueobject;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlideBackground implements Serializable {
    @NonNull
    String type;

    String color;

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
