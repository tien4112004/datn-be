package com.datn.datnbe.ai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for converting hex colors to English color names via TheColorAPI
 * and building theme descriptions from color information.
 */
@Service
@Slf4j
public class ColorDescriptionService {
    private final RestTemplate restTemplate;
    private static final String COLOR_API_BASE_URL = "https://www.thecolorapi.com";

    @Autowired
    public ColorDescriptionService(@Qualifier("colorApiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get English color name from hex color code via TheColorAPI
     * @param hexColor Hex color code (with or without #)
     * @return English color name, or fallback description
     */
    public String getColorName(String hexColor) {
        try {
            // Remove # if present
            String cleanHex = hexColor.replace("#", "");

            String url = COLOR_API_BASE_URL + "/id?hex=" + cleanHex;

            ColorApiResponse response = restTemplate.getForObject(url, ColorApiResponse.class);

            if (response != null && response.getName() != null) {
                return response.getName().getValue();
            }

            log.warn("No color name returned from API for hex: {}", hexColor);
            return "color"; // Fallback

        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling ColorAPI for hex {}: {}", hexColor, e.getMessage());
            return "color";
        } catch (Exception e) {
            log.error("Error calling ColorAPI for hex {}: {}", hexColor, e.getMessage());
            return "color";
        }
    }

    /**
     * Build theme description from theme colors
     * Format: "soft airy [primary] wash, [background] backdrop, [text] accents"
     */
    public String buildThemeDescription(String primaryColor, String backgroundColor, String textColor) {
        String primaryName = getColorName(primaryColor);
        String backgroundName = getColorName(backgroundColor);
        String textName = getColorName(textColor);

        return String.format("soft airy %s wash, %s backdrop, %s accents",
                primaryName.toLowerCase(),
                backgroundName.toLowerCase(),
                textName.toLowerCase());
    }

    /**
     * DTO for ColorAPI response
     */
    @Data
    public static class ColorApiResponse {
        private HexInfo hex;
        private NameInfo name;

        @Data
        public static class HexInfo {
            private String value;
            private String clean;
        }

        @Data
        public static class NameInfo {
            private String value;
            @JsonProperty("closest_named_hex")
            private String closestNamedHex;
        }
    }
}
