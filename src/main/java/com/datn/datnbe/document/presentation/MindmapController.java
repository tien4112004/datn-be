package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.api.MindmapApi;
import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleAndDescriptionRequest;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mindmaps")
@RequiredArgsConstructor
@Slf4j
public class MindmapController {

    private final MindmapApi mindmapApi;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MindmapCreateResponseDto> createMindmap(@Valid @RequestBody MindmapCreateRequest request) {
        log.info("Received request to create mindmap with title: {}", request.getTitle());
        MindmapCreateResponseDto response = mindmapApi.createMindmap(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MindmapListResponseDto>> getAllMindmaps() {
        log.info("Received request to get all mindmaps");
        List<MindmapListResponseDto> response = mindmapApi.getAllMindmaps();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponseDto<MindmapListResponseDto>> getAllMindmapsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String searchQuery) {
        log.info("Received request to get paginated mindmaps - page: {}, size: {}", page, size);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .searchQuery(searchQuery)
                .build();

        PaginatedResponseDto<MindmapListResponseDto> response = mindmapApi.getAllMindmaps(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MindmapDto> getMindmap(@PathVariable String id) {
        log.info("Received request to get mindmap with id: {}", id);
        MindmapDto response = mindmapApi.getMindmap(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateMindmap(@PathVariable String id,
            @Valid @RequestBody MindmapUpdateRequest request) {
        log.info("Received request to update mindmap with id: {}", id);
        mindmapApi.updateMindmap(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/title", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateMindmapTitle(@PathVariable String id,
            @Valid @RequestBody MindmapUpdateTitleAndDescriptionRequest request) {
        log.info("Received request to update mindmap title with id: {}", id);
        mindmapApi.updateTitleAndDescriptionMindmap(id, request);
        return ResponseEntity.noContent().build();
    }
}
