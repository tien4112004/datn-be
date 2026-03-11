package com.datn.datnbe.document.service;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.dto.DocumentMetadataDto;
import com.datn.datnbe.document.dto.request.DocumentCollectionRequest;
import com.datn.datnbe.document.dto.request.RecentDocumentCollectionRequest;
import com.datn.datnbe.document.dto.request.UpdateDocumentRequest;
import com.datn.datnbe.document.dto.response.DocumentMinimalResponseDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.management.ChapterManagement;
import com.datn.datnbe.document.repository.DocumentRepository;
import com.datn.datnbe.sharedkernel.dto.BaseCollectionRequest;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SecurityContextUtils securityContextUtils;
    private final ResourcePermissionApi resourcePermissionApi;
    private final ObjectMapper objectMapper;
    private final ChapterManagement chapterManagement;

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

        DocumentVisit visit = documentRepository
                .findByUserIdAndDocumentId(metadata.getUserId(), metadata.getDocumentId())
                .orElse(DocumentVisit.builder()
                        .userId(metadata.getUserId())
                        .documentId(metadata.getDocumentId())
                        .documentType(metadata.getType())
                        .build());

        visit.setLastVisited(Instant.now());
        visit.setTitle(metadata.getTitle());
        visit.setThumbnail(metadata.getThumbnail());
        documentRepository.save(visit);

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
        return documentRepository.findRecentDocumentsByUser(userId, pageable);
    }

    /**
     * Get recent documents with default pagination
     */
    @Transactional(readOnly = true)
    public Page<DocumentVisit> getRecentDocuments(String userId) {
        Pageable pageable = PageRequest.of(0, BaseCollectionRequest.DEFAULT_PAGE_SIZE);
        return documentRepository.findRecentDocumentsByUser(userId, pageable);
    }

    /**
     * Delete all visits for a document
     */
    @Transactional
    public void deleteDocumentVisits(String documentId) {
        log.info("Deleting all visits for document: {}", documentId);
        documentRepository.deleteByDocumentId(documentId);
    }

    public DocumentMinimalResponseDto getMinimalDocumentInfo(String documentId) {
        log.info("Fetching minimal info for document: {}", documentId);
        return documentRepository.findMinimalDocumentInfoByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }

    public PaginatedResponseDto<JsonNode> getAllDocument(DocumentCollectionRequest request) {

        Sort sortOrder = "asc".equalsIgnoreCase(request.getSort())
                ? Sort.by(Sort.Direction.ASC, "created_at")
                : Sort.by(Sort.Direction.DESC, "created_at");

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sortOrder);

        String ownerId = securityContextUtils.getCurrentUserId();

        List<String> presentationIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "presentation");
        List<String> mindmapIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "mindmap");
        List<String> assignmentIds = resourcePermissionApi.getAllResourceByTypeOfOwner(ownerId, "assignment");

        if (request.getChapter() == null && request.getChapterId() != null) {
            // Has ID only — resolve name but keep only the ID for filtering
            request.setChapter(null);
        } else if (request.getChapter() != null && request.getChapterId() == null) {
            // Has name only — resolve to ID, then clear name so only ID is used in the query
            request.setChapterId(chapterManagement.getChapterId(request.getChapter()));
            request.setChapter(null);
        } else if (request.getChapter() != null && request.getChapterId() != null) {
            // Both provided — prefer ID, clear name to avoid double AND
            request.setChapter(null);
        }

        Page<String> paginatedDocuments = documentRepository.getAllDocuments(pageable,
                request.getFilter(),
                request.getChapter(),
                request.getChapterId(),
                request.getSubject(),
                request.getGrade(),
                presentationIds,
                mindmapIds,
                assignmentIds);

        List<JsonNode> documents = paginatedDocuments.getContent().stream().map(json -> {
            try {
                return objectMapper.readTree(json);
            } catch (Exception e) {
                log.warn("Failed to parse document JSON: {}", e.getMessage());
                return objectMapper.missingNode();
            }
        }).toList();

        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                paginatedDocuments.getTotalElements(), paginatedDocuments.getTotalPages());

        return new PaginatedResponseDto<>(documents, pagination);
    }

    public void updateDocumentChapter(String documentType, String documentId, UpdateDocumentRequest request) {
        log.info("Updating chapter for document - id: {}, type: {}, new chapter: {}",
                documentId,
                documentType,
                request.getChapter());

        if ("presentation".equalsIgnoreCase(documentType)) {
            documentRepository.updatePresentationChapter(documentId,
                    request.getChapterId(),
                    request.getChapter(),
                    request.getSubject(),
                    request.getGrade());
        } else if ("mindmap".equalsIgnoreCase(documentType)) {
            documentRepository.updateMindmapChapter(documentId,
                    request.getChapterId(),
                    request.getChapter(),
                    request.getSubject(),
                    request.getGrade());
        } else if ("assignment".equalsIgnoreCase(documentType)) {
            documentRepository.updateAssignmentChapter(documentId,
                    request.getChapterId(),
                    request.getChapter(),
                    request.getSubject(),
                    request.getGrade());
        } else {
            throw new IllegalArgumentException("Unsupported document type: " + documentType);
        }
    }
}
