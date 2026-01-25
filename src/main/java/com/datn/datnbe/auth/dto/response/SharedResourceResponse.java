package com.datn.datnbe.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for resources shared with the current user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedResourceResponse {
    private String id;
    private String type;           // "mindmap" | "presentation"
    private String title;
    private String permission;     // "read" | "comment"
    private String thumbnailUrl;
    private String ownerId;
    private String ownerName;      // Display name of the owner
}
