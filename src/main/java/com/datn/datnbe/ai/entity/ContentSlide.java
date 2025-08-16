package com.datn.datnbe.ai.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentSlide extends BaseSlide {
    private ContentData data;

    public ContentSlide() {
        super(SlideType.CONTENT);
    }

    @Data
    public static class ContentData {
        private String title;
        private List<ContentItem> items;
    }

    @Data
    public static class ContentItem {
        private String title;
        private String text;
    }
}