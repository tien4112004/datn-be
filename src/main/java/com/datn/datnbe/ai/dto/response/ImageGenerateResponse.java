package com.datn.datnbe.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from generic AI image generation endpoint.
 * Contains base64-encoded image data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateResponse {
    /**
     * List of base64-encoded images (without data URI prefix).
     */
    private List<String> images;

    /**
     * Number of images generated.
     */
    private Integer count;

    /**
     * Timestamp of image generation.
     */
    private String created;
}
