package com.datn.datnbe.document.entity.valueobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlideElement {
    @Field("type")
    String type;

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
}