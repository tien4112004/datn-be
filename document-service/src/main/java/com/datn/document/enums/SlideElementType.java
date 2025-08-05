package com.datn.document.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SlideElementType {
    COVER("cover"),
    TRANSITION("transition"),
    CONTENT("content"),
    CONTENTS("contents"),
    END("end");

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