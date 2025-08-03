package com.datn.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ContentGenerationController {
    @PostMapping(value = "/presentations/mock-outline", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> generateMockOutlineManual() {
        log.info("Received mock outline generation request (manual streaming)");

        // Split the content into words and create a Flux that emits each word with a delay

        return streamWordByWordWithSpaces(
                100,
                Flux.just(mockMarkdownOutline)
                .doOnSubscribe(subscription -> log.info("Starting manual mock outline stream"))
                .doOnComplete(() -> log.info("Mock outline streaming completed"))
                .doOnError(error -> {
                    log.error("Error in manual mock outline streaming", error);
                })
        );
    }

    @GetMapping(value = "/presentations/mock-presentation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateMockPresentation() {
        log.info("Received mock presentation generation request (SSE streaming)");

        // Split the content into words and create a Flux that emits each word with a delay
        return streamWordByWordWithSpaces(
                100,
                Flux.just(mockJsonPresentation)
                        .doOnSubscribe(subscription -> log.info("Starting manual mock outline stream"))
                        .doOnComplete(() -> log.info("Mock outline streaming completed"))
                        .doOnError(error -> {
                            log.error("Error in manual mock outline streaming", error);
                        })
        ).map(content -> ServerSentEvent.<String>builder()
                .data(content)
                .build())
                .concatWith(Mono.just(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("Stream completed")
                        .build()))
                .doOnError(error -> log.error("Error generating outline", error));
    }

    public static Flux<String> streamWordByWordWithSpaces(int delayMillis, Flux<String> source) {
        AtomicReference<String> buffer = new AtomicReference<>("");

        return source
                .flatMap(chunk -> formatChunkWordByWord(chunk, buffer))
                .concatWith(
                        Flux.defer(() -> {
                            String remaining = buffer.get().trim();
                            return remaining.isEmpty() ? Flux.empty() : Flux.just(remaining);
                        })
                ).delayElements(Duration.ofMillis(delayMillis));
    }

    public static Flux<String> formatChunkWordByWord(String chunk, AtomicReference<String> buffer) {
        // Add the new chunk to our buffer
        String currentBuffer = buffer.updateAndGet(existing -> existing + chunk);

        // Split by spaces to get words, but keep the last incomplete word in buffer
        String[] parts = currentBuffer.split(" ");

        if (parts.length <= 1) {
            // No complete words yet, keep everything in buffer
            return Flux.empty();
        }

        // Keep the last part in buffer (might be incomplete)
        String newBuffer = parts[parts.length - 1];
        buffer.set(newBuffer);

        // Emit complete words with trailing spaces
        return Flux.fromArray(Arrays.copyOf(parts, parts.length - 1))
                .filter(word -> !word.isEmpty() && !word.equals("```"))
                .map(word -> word + " "); // Add space after each word
    }

    String mockMarkdownOutline = """
# Quick Presentation: Technology Trends 2024

## Introduction
Welcome to our technology overview presentation.

## Key Areas
- **Artificial Intelligence**: Transforming industries
- **Cloud Computing**: Enabling scalability
- **Cybersecurity**: Protecting digital assets
- **IoT Devices**: Connecting everything

## AI Applications
- Healthcare diagnostics
- Financial analysis
- Manufacturing optimization
- Customer service automation

## Cloud Benefits
- Cost efficiency
- Global accessibility
- Automatic scaling
- Disaster recovery

## Security Challenges
- Data breaches increasing
- Ransomware threats
- Privacy regulations
- Zero-trust architecture

## IoT Growth
- Smart home devices
- Industrial sensors
- Wearable technology
- Connected vehicles

## Conclusion
Technology continues evolving rapidly. Organizations must adapt quickly.

**Thank you for your attention!**
""";

    String mockJsonPresentation = """
    {
        "title": "Quick Presentation: Technology Trends 2024",
        "slides": [
            {
                "title": "Introduction",
                "content": "Welcome to our technology overview presentation."
            },
            {
                "title": "Key Areas",
                "content": "- Artificial Intelligence: Transforming industries\n- Cloud Computing: Enabling scalability\n- Cybersecurity: Protecting digital assets\n- IoT Devices: Connecting everything"
            },
            {
                "title": "AI Applications",
                "content": "- Healthcare diagnostics\n- Financial analysis\n- Manufacturing optimization\n- Customer service automation"
            },
            {
                "title": "Cloud Benefits",
                "content": "- Cost efficiency\n- Global accessibility\n- Automatic scaling\n- Disaster recovery"
            },
            {
                "title": "Security Challenges",
                "content": "- Data breaches increasing\n- Ransomware threats\n- Privacy regulations\n- Zero-trust architecture"
            },
            {
                "title": "IoT Growth",
                "content": "- Smart home devices\n- Industrial sensors\n- Wearable technology\n- Connected vehicles"
            },
            {
                "title": "Conclusion",
                "content": "Technology continues evolving rapidly. Organizations must adapt quickly.\n\n**Thank you for your attention!**"
            }
        ]
    }
    """;
}


