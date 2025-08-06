package com.datn.aiservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransitionSlide extends BaseSlide {
    private TransitionData data;
    
    public TransitionSlide() {
        super(SlideType.TRANSITION);
    }
    
    @Data
    public static class TransitionData {
        private String title;
        private String text;
    }
}