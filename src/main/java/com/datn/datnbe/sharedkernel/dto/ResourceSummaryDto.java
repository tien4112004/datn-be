package com.datn.datnbe.sharedkernel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for resource summary information (id, title, thumbnail).
 * Used when only basic resource metadata is needed without loading the full entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSummaryDto {
    private String id;
    private String title;
    private String thumbnail;
}
