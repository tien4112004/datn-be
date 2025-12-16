package com.datn.datnbe.student.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnrollmentStatus {
    ACTIVE("active"), DROPPED("dropped"), COMPLETED("completed");

    private final String value;

    EnrollmentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EnrollmentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE; // Default value
        }
        for (EnrollmentStatus status : EnrollmentStatus.values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid enrollment status value: " + value);
    }
}
