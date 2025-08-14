package com.datn.datnbe.document.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Base event class with common metadata
 */
@Deprecated
public abstract class BaseEvent {

    private String eventId;
    private LocalDateTime timestamp;
    private String eventType;
    private String source;

    protected BaseEvent(String eventType, String source) {
        this.eventType = eventType;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }
}
