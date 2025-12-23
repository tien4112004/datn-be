package com.datn.datnbe.cms.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LessonStatus {
    DRAFT("DRAFT"), PUBLISHED("PUBLISHED"), ARCHIVED("ARCHIVED");

    private final String value;

    LessonStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LessonStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }
        for (LessonStatus s : LessonStatus.values()) {
            if (s.value.equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid lesson status value: " + value);
    }
}
