package com.datn.datnbe.ai.enums;

public enum ModelType {
    IMAGE, TEXT,;

    public String getName() {
        return this.name().toLowerCase();
    }
}
