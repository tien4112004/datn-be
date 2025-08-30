package com.datn.datnbe.ai.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class StreamingResponseUtils {

    public static final int X_DELAY = 50;
    public static final int LOW_DELAY = 100;
    public static final int MED_DELAY = 150;
    public static final int HIGH_DELAY = 500;
    private static final Pattern DELIM = Pattern.compile("(?m)^\\s*---\\s*$");

    public static Flux<String> streamWordByWordWithSpaces(int delayMillis, Flux<String> source) {
        AtomicReference<String> buffer = new AtomicReference<>("");

        return source.flatMap(chunk -> formatChunkWordByWord(chunk, buffer)).concatWith(Flux.defer(() -> {
            String remaining = buffer.get().trim();
            return remaining.isEmpty() ? Flux.empty() : Flux.just(remaining);
        })).delayElements(Duration.ofMillis(delayMillis));
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

    public static Flux<String> streamByJsonObject(int delayMillis, Flux<String> source) {
        AtomicReference<String> buffer = new AtomicReference<>("");

        return source.concatMap(chunk -> formatChunkByDelimiter(chunk, buffer))
                .concatWith(Flux.defer(() -> {
                    String remaining = buffer.get().trim();
                    if (remaining.isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.just(remaining);
                })).delayElements(Duration.ofMillis(delayMillis));
    }

    public static Flux<String> formatChunkByDelimiter(String chunk, AtomicReference<String> buffer) {
        // Add the new chunk to our buffer
        String currentBuffer = buffer.updateAndGet(existing -> existing + chunk);
        
        // Find all delimiter positions
        String[] parts = DELIM.split(currentBuffer, -1);
        if (parts.length <= 1) {
            // No complete parts yet, keep everything in buffer
            return Flux.empty();
        }
        // All parts except the last are complete

        // Keep the remaining part in buffer
        String newBuffer = parts[parts.length - 1];
        buffer.set(newBuffer);
        
        // Emit all complete parts
        return Flux.fromArray(Arrays.copyOf(parts, parts.length - 1))
                .filter(s -> !s.isEmpty());
    }
}
