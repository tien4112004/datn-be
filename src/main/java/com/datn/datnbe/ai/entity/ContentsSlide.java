package com.datn.datnbe.ai.entity;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentsSlide extends BaseSlide {
    private ContentsData data;

    public ContentsSlide() {
        super(SlideType.CONTENTS);
    }

    @Data
    public static class ContentsData {
        private List<String> items;
    }
}
