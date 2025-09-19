package com.datn.datnbe.sharedkernel.utils;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.stream.StreamSupport;

@Slf4j
public final class JsonUtils {
    private static final ObjectMapper M = new ObjectMapper()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
    private static final ObjectWriter PRETTY = M.writer(new DefaultPrettyPrinter());

    private JsonUtils() {
    }

    // In đẹp một object/array JSON; nếu không parse được thì trả nguyên văn (để debug)
    public static String toPrettyJsonSafe(String raw) {
        if (raw == null)
            return null;
        String s = unwrapIfQuoted(stripFence(raw)).replace("<EOL>", "\n").trim();
        try {
            JsonNode n = M.readTree(s);
            return PRETTY.writeValueAsString(n);
        } catch (Exception e) {
            return s;
        }
    }

    // Trả Flux<String>, mỗi phần tử là 1 slide JSON (string)
    public static Flux<String> splitSlidesAsFlux(String raw) {
        if (raw == null || raw.isBlank()) {
            return Flux.empty();
        }

        String cleanedJson = unwrapIfQuoted(stripFence(raw)).replace("<EOL>", "\n").trim();

        try {
            JsonNode root = M.readTree(cleanedJson);

            // TH1: Đã là 1 slide object riêng lẻ
            if (root.isObject() && root.has("type") && root.has("data")) {
                return Flux.just(M.writeValueAsString(root));
            }

            // TH2: Object chứa slides array: { "slides": [...] }
            if (root.isObject() && root.has("slides")) {
                JsonNode slidesNode = root.get("slides");
                if (slidesNode.isArray()) {
                    return Flux.fromStream(StreamSupport.stream(slidesNode.spliterator(), false).map(slide -> {
                        try {
                            return M.writeValueAsString(slide);
                        } catch (Exception e) {
                            return slide.toString();
                        }
                    }));
                }
            }

            // TH3: Array thuần: [{slide1}, {slide2}, ...]
            if (root.isArray()) {
                return Flux.fromStream(StreamSupport.stream(root.spliterator(), false).map(slide -> {
                    try {
                        return M.writeValueAsString(slide);
                    } catch (Exception e) {
                        return slide.toString();
                    }
                }));
            }

            // Fallback: trả về nguyên văn
            return Flux.just(cleanedJson);

        } catch (Exception e) {
            // Nếu không parse được JSON, trả về nguyên văn
            return Flux.just(cleanedJson);
        }
    }

    public static String stripFence(String t) {
        if (t == null)
            return null;
        String s = t.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl >= 0)
                s = s.substring(firstNl + 1);
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0)
                s = s.substring(0, lastFence);
            s = s.trim();
        }
        return s;
    }

    private static String unwrapIfQuoted(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.length() >= 2 && t.charAt(0) == '"' && t.charAt(t.length() - 1) == '"') {
            try {
                return M.readValue(t, String.class);
            } catch (Exception ignore) {
            }
        }
        return t;
    }
}
