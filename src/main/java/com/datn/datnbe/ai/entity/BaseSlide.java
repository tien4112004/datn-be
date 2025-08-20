package com.datn.datnbe.ai.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({@JsonSubTypes.Type(value = CoverSlide.class, name = "cover"),
        @JsonSubTypes.Type(value = ContentsSlide.class, name = "contents"),
        @JsonSubTypes.Type(value = TransitionSlide.class, name = "transition"),
        @JsonSubTypes.Type(value = ContentSlide.class, name = "content"),
        @JsonSubTypes.Type(value = EndSlide.class, name = "end")})
public abstract class BaseSlide {
    private SlideType type;

    protected BaseSlide(SlideType type) {
        this.type = type;
    }
}