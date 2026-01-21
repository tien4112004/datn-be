package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.document.repository.AssignmentRepository;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedResourceValidationService {

    private final MindmapRepository mindmapRepository;
    private final PresentationRepository presentationRepository;
    private final AssignmentRepository assignmentRepository;

    /**
     * Validates that all linked resources exist.
     * Throws an exception if any resource is not found.
     *
     * @param linkedResources the list of linked resources to validate
     */
    public void validateLinkedResources(List<LinkedResourceDto> linkedResources) {
        if (linkedResources == null || linkedResources.isEmpty()) {
            return;
        }

        List<String> notFoundResources = new ArrayList<>();

        // Group resources by type
        List<String> mindmapIds = linkedResources.stream()
                .filter(r -> "mindmap".equals(r.getType()))
                .map(LinkedResourceDto::getId)
                .collect(Collectors.toList());

        List<String> presentationIds = linkedResources.stream()
                .filter(r -> "presentation".equals(r.getType()))
                .map(LinkedResourceDto::getId)
                .collect(Collectors.toList());

        List<String> assignmentIds = linkedResources.stream()
                .filter(r -> "assignment".equals(r.getType()))
                .map(LinkedResourceDto::getId)
                .collect(Collectors.toList());

        // Validate mindmaps
        if (!mindmapIds.isEmpty()) {
            long foundCount = mindmapRepository.countByIdIn(mindmapIds);
            if (foundCount < mindmapIds.size()) {
                log.warn("Some mindmaps not found. Expected: {}, Found: {}", mindmapIds.size(), foundCount);
                notFoundResources.add("mindmap(s)");
            }
        }

        // Validate presentations
        if (!presentationIds.isEmpty()) {
            long foundCount = presentationRepository.countByIdInAndDeletedAtIsNull(presentationIds);
            if (foundCount < presentationIds.size()) {
                log.warn("Some presentations not found. Expected: {}, Found: {}", presentationIds.size(), foundCount);
                notFoundResources.add("presentation(s)");
            }
        }

        // Validate assignments
        if (!assignmentIds.isEmpty()) {
            long foundCount = assignmentRepository.countByIdIn(assignmentIds);
            if (foundCount < assignmentIds.size()) {
                log.warn("Some assignments not found. Expected: {}, Found: {}", assignmentIds.size(), foundCount);
                notFoundResources.add("assignment(s)");
            }
        }

        if (!notFoundResources.isEmpty()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Some linked resources could not be found: " + String.join(", ", notFoundResources));
        }

        log.info("Successfully validated {} linked resources", linkedResources.size());
    }

    /**
     * Validates the permission level value.
     *
     * @param permissionLevel the permission level to validate
     */
    public void validatePermissionLevel(String permissionLevel) {
        if (permissionLevel != null && !permissionLevel.equals("view") && !permissionLevel.equals("comment")) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid permission level: " + permissionLevel + ". Must be 'view' or 'comment'");
        }
    }

    /**
     * Validates all permission levels in the linked resources list.
     *
     * @param linkedResources the list of linked resources to validate
     */
    public void validateAllPermissionLevels(List<LinkedResourceDto> linkedResources) {
        if (linkedResources == null || linkedResources.isEmpty()) {
            return;
        }

        for (LinkedResourceDto resource : linkedResources) {
            validatePermissionLevel(resource.getPermissionLevel());
        }
    }
}
