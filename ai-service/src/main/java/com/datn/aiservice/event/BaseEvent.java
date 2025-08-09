package com.datn.aiservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Base event class with common metadata
 */
public abstract class BaseEvent {

    private String eventId;
    private LocalDateTime timestamp;

    protected BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
}