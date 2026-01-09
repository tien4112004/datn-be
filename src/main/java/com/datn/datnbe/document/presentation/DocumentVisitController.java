package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.response.RecentDocumentDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import com.datn.datnbe.document.mapper.DocumentVisitMapper;
import com.datn.datnbe.document.service.DocumentVisitService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
            @RequestParam(required = false, defaultValue = "7") String limit) {
        String userId = securityContextUtils.getCurrentUserId();
        log.info("Fetching recent documents for user: {}", userId, limit);

        List<DocumentVisit> recentVisits = documentVisitService.getRecentDocuments(userId, Integer.parseInt(limit));
        
        List<RecentDocumentDto> response = recentVisits.stream()
            .map(documentVisitMapper::toRecentDocumentDto)
            .toList();

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
