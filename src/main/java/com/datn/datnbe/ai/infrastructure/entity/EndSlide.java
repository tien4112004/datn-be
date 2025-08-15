package com.datn.datnbe.ai.infrastructure.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EndSlide extends BaseSlide {

    public EndSlide() {
        super(SlideType.END);
    }
}