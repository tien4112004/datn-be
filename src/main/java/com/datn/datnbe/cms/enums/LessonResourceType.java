package com.datn.datnbe.cms.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LessonResourceType {
    LINK("LINK"), FILE("FILE"), EMBED("EMBED"), OTHER("OTHER");

    private final String value;

    LessonResourceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LessonResourceType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        for (LessonResourceType s : LessonResourceType.values()) {
            if (s.value.equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid lesson resource type value: " + value);
    }
}
