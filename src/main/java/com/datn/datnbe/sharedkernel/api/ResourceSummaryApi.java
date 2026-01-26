package com.datn.datnbe.sharedkernel.api;

import com.datn.datnbe.sharedkernel.dto.ResourceSummaryDto;

import java.util.Collection;
import java.util.Map;

/**
 * API for fetching resource summary information (title, thumbnail) from presentations and mindmaps.
 * This API is in sharedkernel so it can be accessed by both auth and document modules without
 * creating circular dependencies.
 */
public interface ResourceSummaryApi {
    /**
     * Get summaries (id, title, thumbnail) for presentations by their IDs.
     *
     * @param ids Collection of presentation IDs
     * @return Map of presentation ID to ResourceSummaryDto
     */
    Map<String, ResourceSummaryDto> getPresentationSummaries(Collection<String> ids);

    /**
     * Get summaries (id, title, thumbnail) for mindmaps by their IDs.
     *
     * @param ids Collection of mindmap IDs
     * @return Map of mindmap ID to ResourceSummaryDto
     */
    Map<String, ResourceSummaryDto> getMindmapSummaries(Collection<String> ids);
}
