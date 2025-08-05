package com.datn.document.dto;

import com.datn.document.enums.SlideElementType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlideElementDto {
    @NotNull(message = "Element type cannot be null")
    private SlideElementType type;
    private String id;
    private Float left;
    private Float top;
    private Float width;
    private Float height;
    private List<Float> viewBox;
    private String path;
    private String fill;
    private Boolean fixedRatio;
    private Float opacity;
    private Float rotate;
    private Boolean flipV;
    private Float lineHeight;
    private String content;
    private String defaultFontName;
    private String defaultColor;
    private List<Float> start;
    private List<Float> end;
    private List<String> points;
    private String color;
    private String style;
    private Float wordSpace;
}