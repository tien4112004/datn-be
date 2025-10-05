package com.datn.datnbe.document.entity.valueobject;

import com.datn.datnbe.document.enums.SlideElementType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlideElement {
    @Field("type")
    SlideElementType type;

    @Field("id")
    String id;

    @Field("left")
    Float left;

    @Field("top")
    Float top;

    @Field("width")
    Float width;

    @Field("height")
    Float height;

    @Field("viewBox")
    List<Float> viewBox;

    @Field("path")
    String path;

    @Field("fill")
    String fill;

    @Field("fixedRatio")
    Boolean fixedRatio;

    @Field("opacity")
    Float opacity;

    @Field("rotate")
    Float rotate;

    @Field("flipV")
    Boolean flipV;

    @Field("lineHeight")
    Float lineHeight;

    @Field("content")
    String content;

    @Field("defaultFontName")
    String defaultFontName;

    @Field("defaultColor")
    String defaultColor;

    @Field("start")
    List<Float> start;

    @Field("end")
    List<Float> end;

    @Field("points")
    List<String> points;

    @Field("color")
    String color;

    @Field("style")
    String style;

    @Field("wordSpace")
    Float wordSpace;

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
