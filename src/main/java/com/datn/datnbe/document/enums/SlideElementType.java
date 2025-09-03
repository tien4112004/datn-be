package com.datn.datnbe.document.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum SlideElementType {
    text("text"), image("image"), shape("shape"), line("line"), chart("chart"), table("table"), latex("latex"),
    video("video"), audio("audio");

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

    public static SlideElementType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(SlideElementType.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}