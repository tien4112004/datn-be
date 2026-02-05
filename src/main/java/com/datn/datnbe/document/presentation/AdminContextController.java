package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.ContextCollectionRequest;
import com.datn.datnbe.document.dto.request.ContextCreateRequest;
import com.datn.datnbe.document.dto.request.ContextsByIdsRequest;
import com.datn.datnbe.document.dto.request.ContextUpdateRequest;
import com.datn.datnbe.document.dto.response.ContextResponse;
import com.datn.datnbe.document.service.ContextService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contexts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminContextController {

    ContextService contextService;

    /**
     * Get all public contexts (ownerId = null)
     */
    @GetMapping({"", "/"})
    public ResponseEntity<AppResponseDto<List<ContextResponse>>> getAllPublicContexts(
            @Valid @ModelAttribute ContextCollectionRequest request) {

        log.info("GET /api/admin/contexts - Fetching public contexts");

        // Pass null as ownerIdFilter to get only public contexts
        PaginatedResponseDto<ContextResponse> paginatedResponse = contextService.getAllContexts(request, null);

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(paginatedResponse.getData(), paginatedResponse.getPagination()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<ContextResponse>> getContextById(@PathVariable String id) {
        log.info("GET /api/admin/contexts/{} - Fetching context", id);

        ContextResponse response = contextService.getContextById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/by-ids")
    public ResponseEntity<AppResponseDto<List<ContextResponse>>> getContextsByIds(
            @Valid @RequestBody ContextsByIdsRequest request) {

        log.info("POST /api/admin/contexts/by-ids - Fetching {} contexts by IDs", request.getIds().size());

        List<ContextResponse> response = contextService.getContextsByIds(request);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Create a public context (ownerId = null)
     */
    @PostMapping({"", "/"})
    public ResponseEntity<AppResponseDto<ContextResponse>> createPublicContext(
            @Valid @RequestBody ContextCreateRequest request) {

        log.info("POST /api/admin/contexts - Creating public context with title: {}", request.getTitle());

        // Pass null as ownerId to create a public context
        ContextResponse response = contextService.createContext(request, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    /**
     * Update a public context (admin can only modify public contexts)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<ContextResponse>> updateContext(@PathVariable String id,
            @Valid @RequestBody ContextUpdateRequest request) {

        log.info("PUT /api/admin/contexts/{} - Admin updating context", id);

        // Pass null as userId for admin operations
        ContextResponse response = contextService.updateContext(id, request, null);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    /**
     * Delete a public context (admin can only delete public contexts)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContext(@PathVariable String id) {
        log.info("DELETE /api/admin/contexts/{} - Admin deleting context", id);

        // Pass null as userId for admin operations
        contextService.deleteContext(id, null);

        return ResponseEntity.noContent().build();
    }
}
