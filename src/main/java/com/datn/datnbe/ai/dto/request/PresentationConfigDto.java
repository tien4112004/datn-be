package com.datn.datnbe.ai.dto.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Presentation configuration sent from frontend with full theme data.
 * Allows frontend to customize theme colors before generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationConfigDto {

    /** Theme data as a map (includes customized colors from frontend) */
    private Map<String, Object> theme;

    /** Viewport dimensions */
    private ViewportDto viewport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewportDto {
        private Integer width;
        private Integer height;
    }
}
