package com.datn.document.entity.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlideElement {
    @Field("type")
    private String type;

    @Field("id")
    private String id;

    @Field("left")
    private Float left;

    @Field("top")
    private Float top;

    @Field("width")
    private Float width;

    @Field("height")
    private Float height;

    @Field("viewBox")
    private List<Float> viewBox;

    @Field("path")
    private String path;

    @Field("fill")
    private String fill;

    @Field("fixedRatio")
    private Boolean fixedRatio;

    @Field("opacity")
    private Float opacity;

    @Field("rotate")
    private Float rotate;

    @Field("flipV")
    private Boolean flipV;

    @Field("lineHeight")
    private Float lineHeight;

    @Field("content")
    private String content;

    @Field("defaultFontName")
    private String defaultFontName;

    @Field("defaultColor")
    private String defaultColor;

    @Field("start")
    private List<Float> start;

    @Field("end")
    private List<Float> end;

    @Field("points")
    private List<String> points;

    @Field("color")
    private String color;

    @Field("style")
    private String style;

    @Field("wordSpace")
    private Float wordSpace;
}