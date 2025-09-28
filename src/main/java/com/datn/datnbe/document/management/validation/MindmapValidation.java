package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.document.repository.MindmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MindmapValidation {

    private final MindmapRepository mindmapRepository;

    public void validateMindmapExists(String mindmapId) {
        //        if (!mindmapRepository.existsById(mindmapId)) {
        //            throw new ResourceNotFoundException("Mindmap not found with id: " + mindmapId);
        //        }
    }
}
