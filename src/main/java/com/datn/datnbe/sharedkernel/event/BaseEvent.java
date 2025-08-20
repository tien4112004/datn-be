package com.datn.datnbe.sharedkernel.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
/**
 * Base event class with common metadata
 */
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