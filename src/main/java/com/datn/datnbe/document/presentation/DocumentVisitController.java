package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.RecentDocumentCollectionRequest;
import com.datn.datnbe.document.dto.response.RecentDocumentDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.mapper.DocumentVisitMapper;
import com.datn.datnbe.document.service.DocumentVisitService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recent-documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentVisitController {

    DocumentVisitService documentVisitService;
    DocumentVisitMapper documentVisitMapper;
    SecurityContextUtils securityContextUtils;

    @GetMapping
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
}
