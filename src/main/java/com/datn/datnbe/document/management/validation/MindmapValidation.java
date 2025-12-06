package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MindmapValidation {

    private final MindmapRepository mindmapRepository;

    public void validateMindmapExists(String mindmapId) {
        try {
            UUID uuid = UUID.fromString(mindmapId);
            if (!mindmapRepository.existsById(uuid)) {
                throw new ResourceNotFoundException("Mindmap not found with id: " + mindmapId);
            }
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid mindmap id format: " + mindmapId);
        }
    }
}
