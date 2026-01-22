package com.datn.datnbe.cms.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LinkedResourceType {
    MINDMAP("mindmap"), PRESENTATION("presentation"), ASSIGNMENT("assignment");

    private final String value;

    LinkedResourceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LinkedResourceType fromValue(String value) {
        for (LinkedResourceType type : LinkedResourceType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown LinkedResourceType: " + value);
    }
}
