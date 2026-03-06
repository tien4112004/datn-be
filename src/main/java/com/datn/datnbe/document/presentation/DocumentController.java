package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.AssignmentApi;
import com.datn.datnbe.document.api.MindmapApi;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.request.DocumentCollectionRequest;
import com.datn.datnbe.document.dto.request.RecentDocumentCollectionRequest;
import com.datn.datnbe.document.dto.response.DocumentMinimalResponseDto;
import com.datn.datnbe.document.dto.response.RecentDocumentDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.mapper.DocumentVisitMapper;
import com.datn.datnbe.document.service.DocumentService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentController {

    DocumentService documentVisitService;
    DocumentVisitMapper documentVisitMapper;
    SecurityContextUtils securityContextUtils;
    PresentationApi presentationApi;
    MindmapApi mindmapApi;
    AssignmentApi assignmentApi;

    @GetMapping("/recent-documents")
    public ResponseEntity<AppResponseDto<List<RecentDocumentDto>>> getRecentDocuments(
            @Valid @ModelAttribute RecentDocumentCollectionRequest request) {
        String userId = securityContextUtils.getCurrentUserId();
        log.info("Fetching recent documents for user: {}, page: {}, pageSize: {}",
                userId,
                request.getPage(),
                request.getPageSize());

        Page<DocumentVisit> recentVisits = documentVisitService.getRecentDocuments(userId, request);

        List<RecentDocumentDto> response = recentVisits.getContent()
                .stream()
                .map(documentVisitMapper::toRecentDocumentDto)
                .toList();

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(request.getPage())
                .pageSize(request.getPageSize())
                .totalItems(recentVisits.getTotalElements())
                .totalPages(recentVisits.getTotalPages())
                .build();

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response, pagination));
    }

    @GetMapping("/documents/{documentId}/minimal")
    public ResponseEntity<AppResponseDto<DocumentMinimalResponseDto>> getMinimalDocumentInfo(
            @PathVariable String documentId) {
        log.info("Fetching minimal info for document: {}", documentId);
        var documentInfo = documentVisitService.getMinimalDocumentInfo(documentId);
        return ResponseEntity.ok(AppResponseDto.success(documentInfo));
    }

    @GetMapping("/documents")
    public ResponseEntity<AppResponseDto<List<Object>>> getAllDocuments(
        @Valid @ModelAttribute DocumentCollectionRequest request) {
    
        int pageSize = request.getPageSize();
        int pageNum = request.getPage();
        
        // Fetch all documents from all sources with page size 1 to collect all available items
        List<Object> allDocuments = new ArrayList<>();
        
        var presentations = presentationApi.getAllPresentations(request);
        var mindmaps = mindmapApi.getAllMindmaps(request);
        var assignments = assignmentApi.getAssignments(request);
        
        // Combine all documents into a single list
        if (presentations != null && presentations.getData() != null) {
            allDocuments.addAll(presentations.getData());
        }
        if (mindmaps != null && mindmaps.getData() != null) {
            allDocuments.addAll(mindmaps.getData());
        }
        if (assignments != null && assignments.getData() != null) {
            allDocuments.addAll(assignments.getData());
        }
        
        // Calculate pagination for the combined list
        int totalItems = allDocuments.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        List<Object> paginatedDocuments = startIndex < totalItems 
            ? allDocuments.subList(startIndex, endIndex) 
            : new ArrayList<>();
        
        PaginationDto pagination = PaginationDto.builder()
                .currentPage(pageNum)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build();

        return ResponseEntity.ok(AppResponseDto.successWithPagination(paginatedDocuments, pagination));
    }
}
