package com.datn.datnbe.student.exam.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GradeLevel {
    K("K"), GRADE_1("1"), GRADE_2("2"), GRADE_3("3"), GRADE_4("4"), GRADE_5("5");

    private final String value;

    GradeLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static GradeLevel fromValue(String value) {
        for (GradeLevel level : GradeLevel.values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid grade level: " + value);
    }
}
