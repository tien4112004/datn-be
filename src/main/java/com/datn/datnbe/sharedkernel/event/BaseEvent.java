package com.datn.datnbe.sharedkernel.event;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** Base event class with common metadata */
public abstract class BaseEvent {

    private String eventId;
    private Date timestamp;
    private String eventType;
    private String source;

    protected BaseEvent(String eventType, String source) {
        this.eventType = eventType;
        this.source = source;
        this.timestamp = new Date();
    }
}
