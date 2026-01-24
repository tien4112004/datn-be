package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.DocumentValidationApi;
import com.datn.datnbe.document.repository.AssignmentRepository;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.document.repository.PresentationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Management service for document validation operations.
 * Provides methods to check existence of various document types.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentValidationManagement implements DocumentValidationApi {

    private final MindmapRepository mindmapRepository;
    private final PresentationRepository presentationRepository;
    private final AssignmentRepository assignmentRepository;

    @Override
    public long countExistingMindmaps(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return mindmapRepository.countByIdIn(ids);
    }

    @Override
    public long countExistingPresentations(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return presentationRepository.countByIdInAndDeletedAtIsNull(ids);
    }

    @Override
    public long countExistingAssignments(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return assignmentRepository.countByIdIn(ids);
    }
}
