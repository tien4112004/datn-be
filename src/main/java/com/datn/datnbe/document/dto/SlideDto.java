package com.datn.datnbe.document.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlideDto {
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
        String type;

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

        Map<String, Object> extraFields = new java.util.HashMap<>();

        @JsonAnySetter
        public void setExtraField(String key, Object value) {
            extraFields.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getExtraFields() {
            return extraFields;
        }
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
