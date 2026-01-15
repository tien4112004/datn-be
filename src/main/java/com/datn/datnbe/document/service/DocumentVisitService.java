package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.DocumentMetadataDto;
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
     * Updates last visited time with title and thumbnail from metadata
     */
    @Async
    @Transactional
    public void trackDocumentVisit(DocumentMetadataDto metadata) {
        log.info("Tracking visit - userId: {}, documentId: {}, type: {}",
                metadata.getUserId(),
                metadata.getDocumentId(),
                metadata.getType());

        DocumentVisit visit = visitRepository.findByUserIdAndDocumentId(metadata.getUserId(), metadata.getDocumentId())
                .orElse(DocumentVisit.builder()
                        .userId(metadata.getUserId())
                        .documentId(metadata.getDocumentId())
                        .documentType(metadata.getType())
                        .build());

        visit.setLastVisited(LocalDateTime.now());
        visit.setTitle(metadata.getTitle());
        visit.setThumbnail(metadata.getThumbnail());
        visitRepository.save(visit);

        log.debug("Visit tracked successfully for userId: {}, documentId: {}, title: {}",
                metadata.getUserId(),
                metadata.getDocumentId(),
                metadata.getTitle());
    }

    /**
     * Get recent documents (custom limit) visited by user
     */
    @Transactional(readOnly = true)
    public List<DocumentVisit> getRecentDocuments(String userId, int limit) {
        log.info("Fetching recent documents for user: {}, limit: {}", userId, limit);

        Pageable pageable = PageRequest.of(0, limit > 0 ? limit : DEFAULT_LIMIT);
        List<DocumentVisit> a = visitRepository.findRecentDocumentsByUser(userId, pageable);
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
