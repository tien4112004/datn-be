package com.datn.document.entity.valueObject;

import java.util.List;

import lombok.Getter;

@Getter
public class Slide {
    // Maybe remove cause MongoDB will handle this
    private String id;
    // Will be replaced with actual elements
    private List<String> elements;
    // Will be replaced with actual background
    private String background;
}
