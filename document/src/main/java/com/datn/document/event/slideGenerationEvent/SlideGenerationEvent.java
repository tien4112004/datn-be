package com.datn.document.event.slideGenerationEvent;

import com.datn.document.event.BaseEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Event for slide generation operations
 */
public class SlideGenerationEvent extends BaseEvent {
    
    private String presentationId;
    private String status;
    
    public SlideGenerationEvent(String presentationId, String status) {
        super("SLIDE_GENERATION", "document-service");
        this.presentationId = presentationId;
        this.status = status;
    }
}
