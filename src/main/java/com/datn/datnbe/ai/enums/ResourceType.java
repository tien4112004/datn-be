package com.datn.datnbe.ai.enums;

public enum ResourceType {
    PRESENTATION, SLIDE, IMAGE, MINDMAP, QUESTION, ASSIGNMENT, OUTLINE;

    public String getDisplayName() {
        return switch (this) {
            case PRESENTATION -> "Presentation";
            case SLIDE -> "Slide";
            case IMAGE -> "Image";
            case MINDMAP -> "Mindmap";
            case QUESTION -> "Question";
            case ASSIGNMENT -> "Assignment";
            case OUTLINE -> "Outline";
        };
    }
}
