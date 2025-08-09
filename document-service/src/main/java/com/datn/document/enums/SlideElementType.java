package com.datn.document.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SlideElementType {
    TEXT("text"), IMAGE("image"), SHAPE("shape"), LINE("line"), CHART("chart"), TABLE("table"), LATEX("latex"),
    VIDEO("video"), AUDIO("audio");

    private final String value;

    SlideElementType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}