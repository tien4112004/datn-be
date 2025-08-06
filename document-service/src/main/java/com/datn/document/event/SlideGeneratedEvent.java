package com.datn.document.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Event for slide Generated operations
 */
public class SlideGeneratedEvent extends BaseEvent {
    
    private String presentationId;
    private String status;
    
    public SlideGeneratedEvent(String presentationId, String status) {
        super("SLIDE_Generated", "document-service");
        this.presentationId = presentationId;
        this.status = status;
    }
}
