package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.request.RecentDocumentCollectionRequest;
import com.datn.datnbe.document.dto.response.DocumentMinimalResponseDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.repository.DocumentVisitRepository;
import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentVisitRepository visitRepository;

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

        visit.setLastVisited(Instant.now());
        visit.setTitle(metadata.getTitle());
        visit.setThumbnail(metadata.getThumbnail());
        visitRepository.save(visit);

        log.debug("Visit tracked successfully for userId: {}, documentId: {}, title: {}",
                metadata.getUserId(),
                metadata.getDocumentId(),
                metadata.getTitle());
    }

    /**
     * Get recent documents visited by user with pagination
     */
    @Transactional(readOnly = true)
    public Page<DocumentVisit> getRecentDocuments(String userId, RecentDocumentCollectionRequest request) {
        log.info("Fetching recent documents for user: {}, page: {}, pageSize: {}",
                userId,
                request.getPage(),
                request.getPageSize());

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize());
        return visitRepository.findRecentDocumentsByUser(userId, pageable);
    }

    /**
     * Get recent documents with default pagination
     */
    @Transactional(readOnly = true)
    public Page<DocumentVisit> getRecentDocuments(String userId) {
        Pageable pageable = PageRequest.of(0, BaseCollectionRequest.DEFAULT_PAGE_SIZE);
        return visitRepository.findRecentDocumentsByUser(userId, pageable);
    }

    /**
     * Delete all visits for a document
     */
    @Transactional
    public void deleteDocumentVisits(String documentId) {
        log.info("Deleting all visits for document: {}", documentId);
        visitRepository.deleteByDocumentId(documentId);
    }

    public DocumentMinimalResponseDto getMinimalDocumentInfo(String documentId) {
        log.info("Fetching minimal info for document: {}", documentId);
        return visitRepository.findMinimalDocumentInfoByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }
}
