package com.datn.datnbe.document.enums;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonCreator
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