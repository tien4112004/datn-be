package com.datn.datnbe.student.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"), FEMALE("female"), OTHER("other");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value.trim())) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender value: " + value);
    }
}
