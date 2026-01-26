package com.datn.datnbe.document.management;

import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.api.ResourceSummaryApi;
import com.datn.datnbe.sharedkernel.dto.ResourceSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceSummaryManagement implements ResourceSummaryApi {
    private final PresentationRepository presentationRepository;
    private final MindmapRepository mindmapRepository;

    @Override
    public Map<String, ResourceSummaryDto> getPresentationSummaries(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        Map<String, ResourceSummaryDto> result = new HashMap<>();
        presentationRepository.findSummariesByIds(ids)
                .forEach(s -> result.put(s.getId(),
                        ResourceSummaryDto.builder()
                                .id(s.getId())
                                .title(s.getTitle())
                                .thumbnail(s.getThumbnail())
                                .build()));

        log.debug("Fetched {} presentation summaries for {} IDs", result.size(), ids.size());
        return result;
    }

    @Override
    public Map<String, ResourceSummaryDto> getMindmapSummaries(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        Map<String, ResourceSummaryDto> result = new HashMap<>();
        mindmapRepository.findSummariesByIds(ids)
                .forEach(s -> result.put(s.getId(),
                        ResourceSummaryDto.builder()
                                .id(s.getId())
                                .title(s.getTitle())
                                .thumbnail(s.getThumbnail())
                                .build()));

        log.debug("Fetched {} mindmap summaries for {} IDs", result.size(), ids.size());
        return result;
    }
}
