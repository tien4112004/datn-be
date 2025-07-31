package com.datn.aiservice.event;

import java.util.List;

import com.datn.aiservice.entity.BaseSlide;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PresentationGeneratedEvent extends BaseEvent {

    List<BaseSlide> payload;

    public PresentationGeneratedEvent(List<BaseSlide> slides) {
        super();
        this.payload = slides;
    }
}
