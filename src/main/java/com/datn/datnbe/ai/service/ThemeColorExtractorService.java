package com.datn.datnbe.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for extracting key colors from slide theme data.
 * Supports both solid colors and gradient backgrounds.
 */
@Service
@Slf4j
public class ThemeColorExtractorService {

    /**
     * Extract primary color from theme
     * Source: themeColors[0] (first color in theme colors array)
     *
     * @param themeData Theme data map
     * @return Primary color in hex format, or fallback color
     */
    public String extractPrimaryColor(Map<String, Object> themeData) {
        try {
            @SuppressWarnings("unchecked") List<String> themeColors = (List<String>) themeData.get("themeColors");
            if (themeColors != null && !themeColors.isEmpty()) {
                return themeColors.get(0); // First color is primary
            }
        } catch (Exception e) {
            log.error("Error extracting primary color", e);
        }
        return "#1e40af"; // Fallback: default blue
    }

    /**
     * Extract background color from theme
     * Handles both solid colors and gradients
     * For gradients, uses Option A: first color (MVP approach)
     *
     * @param themeData Theme data map
     * @return Background color in hex format, or fallback color
     */
    public String extractBackgroundColor(Map<String, Object> themeData) {
        try {
            Object backgroundColor = themeData.get("backgroundColor");

            // Case 1: Solid color (string)
            if (backgroundColor instanceof String) {
                return (String) backgroundColor;
            }

            // Case 2: Gradient (map/object)
            if (backgroundColor instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> gradient = (Map<String, Object>) backgroundColor;
                @SuppressWarnings("unchecked") List<Map<String, Object>> colors = (List<Map<String, Object>>) gradient
                        .get("colors");

                if (colors != null && !colors.isEmpty()) {
                    // Option A: Return first color (simplest - MVP approach)
                    Object firstColor = colors.get(0).get("color");
                    if (firstColor instanceof String) {
                        return (String) firstColor;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting background color", e);
        }
        return "#ffffff"; // Fallback: white
    }

    /**
     * Extract text color from theme
     * Source: fontColor field
     *
     * @param themeData Theme data map
     * @return Text color in hex format, or fallback color
     */
    public String extractTextColor(Map<String, Object> themeData) {
        try {
            Object fontColor = themeData.get("fontColor");
            if (fontColor instanceof String) {
                String color = (String) fontColor;
                if (!color.isEmpty()) {
                    return color;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting text color", e);
        }
        return "#000000"; // Fallback: black
    }

    /**
     * Option B: Find dominant color in gradient (largest area)
     * Can be used in future enhancement if needed
     *
     * @param colors Gradient colors list
     * @return Dominant color in hex format
     */
    @SuppressWarnings("unused")
    private String findDominantGradientColor(List<Map<String, Object>> colors) {
        if (colors == null || colors.isEmpty()) {
            return "#ffffff";
        }

        if (colors.size() == 1) {
            Object color = colors.get(0).get("color");
            return color instanceof String ? (String) color : "#ffffff";
        }

        // Sort by position
        List<Map<String, Object>> sortedColors = colors.stream().sorted((a, b) -> {
            int posA = ((Number) a.get("pos")).intValue();
            int posB = ((Number) b.get("pos")).intValue();
            return Integer.compare(posA, posB);
        }).toList();

        int maxRange = 0;
        String dominantColor = (String) sortedColors.get(0).get("color");

        for (int i = 0; i < sortedColors.size() - 1; i++) {
            int pos1 = ((Number) sortedColors.get(i).get("pos")).intValue();
            int pos2 = ((Number) sortedColors.get(i + 1).get("pos")).intValue();
            int range = pos2 - pos1;

            if (range > maxRange) {
                maxRange = range;
                Object color = sortedColors.get(i).get("color");
                if (color instanceof String) {
                    dominantColor = (String) color;
                }
            }
        }

        return dominantColor;
    }

    /**
     * Option C: Find color closest to middle position (50%)
     * Can be used in future enhancement if needed
     *
     * @param colors Gradient colors list
     * @return Middle color in hex format
     */
    @SuppressWarnings("unused")
    private String findMiddleGradientColor(List<Map<String, Object>> colors) {
        if (colors == null || colors.isEmpty()) {
            return "#ffffff";
        }

        int target = 50;
        Map<String, Object> closestColor = colors.get(0);
        int minDiff = Math.abs(((Number) closestColor.get("pos")).intValue() - target);

        for (Map<String, Object> color : colors) {
            int pos = ((Number) color.get("pos")).intValue();
            int diff = Math.abs(pos - target);
            if (diff < minDiff) {
                minDiff = diff;
                closestColor = color;
            }
        }

        Object color = closestColor.get("color");
        return color instanceof String ? (String) color : "#ffffff";
    }
}
