package com.datn.datnbe.ai.enums;

public enum UnitType {
    PER_REQUEST, PER_SLIDE, PER_IMAGE, PER_QUESTION;

    public String getDisplayName() {
        return switch (this) {
            case PER_REQUEST -> "Per Request";
            case PER_SLIDE -> "Per Slide";
            case PER_IMAGE -> "Per Image";
            case PER_QUESTION -> "Per Question";
        };
    }
}
