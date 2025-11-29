package com.datn.datnbe.student.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StudentStatus {
    ACTIVE("active"), INACTIVE("inactive"), GRADUATED("graduated"), TRANSFERRED("transferred"), SUSPENDED("suspended");

    private final String value;

    StudentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StudentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE; // Default value
        }
        for (StudentStatus status : StudentStatus.values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
