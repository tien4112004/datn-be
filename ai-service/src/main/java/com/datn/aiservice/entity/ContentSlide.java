package com.datn.aiservice.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

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