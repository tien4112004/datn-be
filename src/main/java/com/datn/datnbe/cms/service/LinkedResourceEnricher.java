package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.sharedkernel.api.ResourceSummaryApi;
import com.datn.datnbe.sharedkernel.dto.ResourceSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that enriches LinkedResourceDto instances with title and thumbnail
 * by batch-fetching resource summaries from the database.
 * This eliminates N+1 API calls on the frontend/mobile.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedResourceEnricher {

    private final ResourceSummaryApi resourceSummaryApi;

    /**
     * Enriches a list of LinkedResourceDto with title and thumbnail.
     * Groups resources by type and batch-fetches summaries for each type.
     *
     * @param linkedResources List of LinkedResourceDto to enrich (modified in place)
     */
    public void enrichLinkedResources(List<LinkedResourceDto> linkedResources) {
        if (linkedResources == null || linkedResources.isEmpty()) {
            return;
        }

        // Group resources by type
        Map<String, List<LinkedResourceDto>> byType = linkedResources.stream()
                .collect(Collectors.groupingBy(LinkedResourceDto::getType));

        // Batch fetch and enrich presentations
        List<LinkedResourceDto> presentations = byType.get("presentation");
        if (presentations != null && !presentations.isEmpty()) {
            List<String> ids = presentations.stream().map(LinkedResourceDto::getId).toList();
            Map<String, ResourceSummaryDto> summaries = resourceSummaryApi.getPresentationSummaries(ids);
            enrichFromSummaries(presentations, summaries);
            log.debug("Enriched {} presentations", presentations.size());
        }

        // Batch fetch and enrich mindmaps
        List<LinkedResourceDto> mindmaps = byType.get("mindmap");
        if (mindmaps != null && !mindmaps.isEmpty()) {
            List<String> ids = mindmaps.stream().map(LinkedResourceDto::getId).toList();
            Map<String, ResourceSummaryDto> summaries = resourceSummaryApi.getMindmapSummaries(ids);
            enrichFromSummaries(mindmaps, summaries);
            log.debug("Enriched {} mindmaps", mindmaps.size());
        }

        // Batch fetch and enrich assignments
        List<LinkedResourceDto> assignments = byType.get("assignment");
        if (assignments != null && !assignments.isEmpty()) {
            List<String> ids = assignments.stream().map(LinkedResourceDto::getId).toList();
            Map<String, ResourceSummaryDto> summaries = resourceSummaryApi.getAssignmentSummaries(ids);
            enrichFromSummaries(assignments, summaries);
            log.debug("Enriched {} assignments", assignments.size());
        }
    }

    private void enrichFromSummaries(List<LinkedResourceDto> resources, Map<String, ResourceSummaryDto> summaries) {
        for (LinkedResourceDto resource : resources) {
            ResourceSummaryDto summary = summaries.get(resource.getId());
            if (summary != null) {
                resource.setTitle(summary.getTitle());
                resource.setThumbnail(summary.getThumbnail());
            }
        }
    }
}
