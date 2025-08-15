package com.datn.datnbe.document.dto;

import com.datn.datnbe.document.infrastructure.enums.SlideElementType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlideDto {
    @NotBlank(message = "Slide ID cannot be blank")
    String id;

    @Valid
    List<SlideElementDto> elements;

    @Valid
    SlideBackgroundDto background;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SlideElementDto {
        @NotNull(message = "Element type cannot be null")
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SlideBackgroundDto {
        String type;
        String color;
    }
}