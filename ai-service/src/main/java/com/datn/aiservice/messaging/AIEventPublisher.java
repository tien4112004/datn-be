package com.datn.aiservice.messaging;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.datn.aiservice.event.PresentationGeneratedEvent;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIEventPublisher {
    StreamBridge streamBridge;

    public void publishEvent(PresentationGeneratedEvent event) {
        String routingKey = "presentation.generated." + event.getClass().getSimpleName().toLowerCase();
        try {
            Message<PresentationGeneratedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader("routingKey", routingKey)
                    .setHeader("eventType", event.getClass().getSimpleName().toLowerCase())
                    .setHeader("eventId", event.getEventId())
                    .setHeader("eventTimestamp", event.getTimestamp().toString())
                    .setHeader("source", "ai-service")
                    .build();
            
            boolean sent = streamBridge.send("ai-events-out-0", message);
            
            if (sent) {
                log.info("Successfully published {} with routing key: {}", 
                           event.getClass(), routingKey);
            } else {
                log.error("Failed to publish {} with routing key: {}", 
                            event.getClass(), routingKey);
            }
        } catch (Exception e) {
            log.error("Error publishing event {} with routing key {}: ", 
                        event.getClass(), routingKey);
            // Optional: Implement retry mechanism or dead letter queue
            throw new RuntimeException("Failed to publish AI event", e);
        }
    }
}
