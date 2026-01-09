package com.datn.datnbe.document.service;

import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.repository.DocumentVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
    import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentVisitService {

    private final DocumentVisitRepository visitRepository;
    private static final int DEFAULT_LIMIT = 7;

    /**
     * Track document visit asynchronously (background job)
     * Updates last visited time
     */
    @Async
    @Transactional
    public void trackDocumentVisit(String userId, String documentId, String documentType) {
        log.info("Tracking visit (async) - userId: {}, documentId: {}, type: {}", userId, documentId, documentType);
        
        DocumentVisit visit = visitRepository
            .findByUserIdAndDocumentId(userId, documentId)
            .orElse(DocumentVisit.builder()
                .userId(userId)
                .documentId(documentId)
                .documentType(documentType)
                .build());
        
        visit.setLastVisited(LocalDateTime.now());
        visitRepository.save(visit);
        
        log.debug("Visit tracked successfully for userId: {}, documentId: {}", userId, documentId);
    }

    /**
     * Get recent documents (custom limit) visited by user
     */
    @Transactional(readOnly = true)
    public List<DocumentVisit> getRecentDocuments(String userId, int limit) {
        log.info("Fetching recent documents for user: {}, limit: {}", userId, limit);
        
        Pageable pageable = PageRequest.of(0, limit > 0 ? limit : DEFAULT_LIMIT);
        return visitRepository.findRecentDocumentsByUser(userId, pageable);
    }

    /**
     * Get recent documents with default limit of 7
     */
    @Transactional(readOnly = true)
    public List<DocumentVisit> getRecentDocuments(String userId) {
        return getRecentDocuments(userId, DEFAULT_LIMIT);
    }

    /**
     * Delete all visits for a document
     */
    @Transactional
    public void deleteDocumentVisits(String documentId) {
        log.info("Deleting all visits for document: {}", documentId);
        visitRepository.deleteByDocumentId(documentId);
    }
}
