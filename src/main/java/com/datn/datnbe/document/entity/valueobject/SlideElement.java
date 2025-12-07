package com.datn.datnbe.document.entity.valueobject;

import com.datn.datnbe.document.enums.SlideElementType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlideElement implements Serializable {
    SlideElementType type;

    String id;

    Float left;

    Float top;

    Float width;

    Float height;

    List<Float> viewBox;

    String path;

    String fill;

    Boolean fixedRatio;

    Float opacity;

    Float rotate;

    Boolean flipV;

    Float lineHeight;

    String content;

    String defaultFontName;

    String defaultColor;

    List<Float> start;

    List<Float> end;

    List<String> points;

    String color;

    String style;

    Float wordSpace;

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
