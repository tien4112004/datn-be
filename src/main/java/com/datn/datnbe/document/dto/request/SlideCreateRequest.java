package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlideCreateRequest {

    @Valid
    List<SlideElementCreateRequest> elements;

    @Valid
    SlideDto.SlideBackgroundDto background;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SlideElementCreateRequest {
        @NotNull(message = "Element type cannot be null")
        String type;
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

}