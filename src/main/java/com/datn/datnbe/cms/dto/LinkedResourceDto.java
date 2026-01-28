package com.datn.datnbe.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedResourceDto {
    @NotBlank
    @Pattern(regexp = "^(mindmap|presentation|assignment)$", message = "Type must be mindmap, presentation, or assignment")
    private String type;

    @NotBlank
    private String id;

    @Builder.Default
    private String permissionLevel = "view"; // "view" or "comment"

    // Enriched fields - populated by LinkedResourceEnricher
    private String title;      // Resource title (nullable for backward compatibility)
    private String thumbnail;  // Resource thumbnail URL (nullable, always null for assignments)
}
