package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.request.ContextCreateRequest;
import com.datn.datnbe.document.dto.request.ContextUpdateRequest;
import com.datn.datnbe.document.dto.response.ContextResponse;
import com.datn.datnbe.document.service.ContextService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contexts")
@RequiredArgsConstructor
@Slf4j
public class ContextController {

    private final ContextService contextService;

    @GetMapping
    public ResponseEntity<AppResponseDto<List<ContextResponse>>> getAllContexts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/contexts - Fetching contexts with page: {}, size: {}", page, size);
        


        Page<ContextResponse> contextPage = contextService.getAllContexts(page, size);

        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(contextPage.getNumber())
                .pageSize(contextPage.getSize())
                .totalItems(contextPage.getTotalElements())
                .totalPages(contextPage.getTotalPages())
                .build();

        return ResponseEntity.ok(
                AppResponseDto.successWithPagination(contextPage.getContent(), paginationDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto<ContextResponse>> getContextById(@PathVariable String id) {
        log.info("GET /api/contexts/{} - Fetching context", id);
        
        ContextResponse response = contextService.getContextById(id);
        
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping
    public ResponseEntity<AppResponseDto<ContextResponse>> createContext(
            @Valid @RequestBody ContextCreateRequest request) {
        
        log.info("POST /api/contexts - Creating context with content: {}", request.getContent());
        
        ContextResponse response = contextService.createContext(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppResponseDto<ContextResponse>> updateContext(
            @PathVariable String id,
            @Valid @RequestBody ContextUpdateRequest request) {
        
        log.info("PUT /api/contexts/{} - Updating context", id);
        
        ContextResponse response = contextService.updateContext(id, request);
        
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponseDto<Void>> deleteContext(@PathVariable String id) {
        log.info("DELETE /api/contexts/{} - Deleting context", id);
        
        contextService.deleteContext(id);
        
        return ResponseEntity.ok(AppResponseDto.success(null));
    }
}
