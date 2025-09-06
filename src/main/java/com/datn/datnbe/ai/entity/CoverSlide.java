package com.datn.datnbe.ai.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoverSlide extends BaseSlide {
    private CoverData data;

    public CoverSlide() {
        super(SlideType.COVER);
    }

    @Data
    public static class CoverData {
        private String title;
        private String text;
    }
}
