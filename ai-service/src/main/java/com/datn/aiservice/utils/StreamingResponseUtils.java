package com.datn.aiservice.utils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class StreamingResponseUtils {

    public static final int X_DELAY = 50;
    public static final int LOW_DELAY = 100;
    public static final int MED_DELAY = 150;
    public static final int HIGH_DELAY = 200;

    public static Flux<String> streamWordByWordWithSpaces(int delayMillis, Flux<String> source) {
        AtomicReference<String> buffer = new AtomicReference<>("");

        return source
                .flatMap(chunk -> formatChunkWordByWord(chunk, buffer))
                .concatWith(
                        Flux.defer(() -> {
                            String remaining = buffer.get().trim();
                            return remaining.isEmpty() ? Flux.empty() : Flux.just(remaining);
                        }))
                .delayElements(Duration.ofMillis(delayMillis));
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
}
