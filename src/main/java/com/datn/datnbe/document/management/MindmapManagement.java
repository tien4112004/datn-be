package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MindmapApi;
import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleRequest;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.document.entity.Mindmap;
import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.mapper.MindmapEntityMapper;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.document.management.validation.MindmapValidation;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class MindmapManagement implements MindmapApi {

    private final MindmapRepository mindmapRepository;
    private final MindmapEntityMapper mapper;
    private final MindmapValidation validation;

    @Override
    public MindmapCreateResponseDto createMindmap(MindmapCreateRequest request) {
        log.info("Creating mindmap with title: '{}'", request.getTitle());

        try {
            Mindmap mindmap = buildMindmapFromRequest(request);
            Mindmap savedMindmap = mindmapRepository.save(mindmap);

            log.info("Successfully created mindmap with id: '{}'", savedMindmap.getId());
            return MindmapCreateResponseDto.builder().id(savedMindmap.getId()).build();
        } catch (Exception e) {
            log.error("Failed to create mindmap with title: '{}'. Error: {}", request.getTitle(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MindmapListResponseDto> getAllMindmaps() {
        log.info("Retrieving all mindmaps");

        try {
            List<Mindmap> mindmaps = mindmapRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

            List<MindmapListResponseDto> response = mindmaps.stream()
                    .map(mapper::entityToListResponse)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} mindmaps", response.size());
            return response;
        } catch (Exception e) {
            log.error("Failed to retrieve all mindmaps. Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<MindmapListResponseDto> getAllMindmaps(MindmapCollectionRequest request) {
        log.info("Retrieving mindmaps with pagination - page: {}, size: {}", request.getPage(), request.getSize());

        try {
            Pageable pageable = PageRequest
                    .of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<Mindmap> mindmapPage = mindmapRepository.findAll(pageable);

            List<MindmapListResponseDto> content = mindmapPage.getContent()
                    .stream()
                    .map(mapper::entityToListResponse)
                    .collect(Collectors.toList());

            PaginationDto pagination = new PaginationDto(mindmapPage.getNumber(), mindmapPage.getSize(),
                    mindmapPage.getTotalElements(), mindmapPage.getTotalPages());

            log.info("Successfully retrieved {} mindmaps from page {} of {}",
                    content.size(),
                    pagination.getCurrentPage(),
                    pagination.getTotalPages());

            return new PaginatedResponseDto<>(content, pagination);
        } catch (Exception e) {
            log.error("Failed to retrieve paginated mindmaps. Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateMindmap(String id, MindmapUpdateRequest request) {
        log.info("Updating mindmap with id: '{}'", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap existingMindmap = findMindmapById(id);
            mapper.updateEntityFromRequest(request, existingMindmap);

            mindmapRepository.save(existingMindmap);
            log.info("Successfully updated mindmap with id: '{}'", id);
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update mindmap with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateTitleMindmap(String id, MindmapUpdateTitleRequest request) {
        log.info("Updating mindmap title with id: '{}'", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap existingMindmap = findMindmapById(id);

            if (StringUtils.hasText(request.getTitle())) {
                existingMindmap.setTitle(request.getTitle());
            }

            if (StringUtils.hasText(request.getDescription())) {
                existingMindmap.setDescription(request.getDescription());
            }

            existingMindmap.setUpdatedAt(LocalDateTime.now());

            mindmapRepository.save(existingMindmap);
            log.info("Successfully updated mindmap title with id: '{}'", id);
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update mindmap title with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MindmapDto getMindmap(String id) {
        log.info("Retrieving mindmap with id: '{}'", id);

        try {
            validation.validateMindmapExists(id);

            Mindmap mindmap = findMindmapById(id);
            MindmapDto response = mapper.entityToDto(mindmap);

            log.info("Successfully retrieved mindmap with id: '{}'", id);
            return response;
        } catch (ResourceNotFoundException e) {
            log.error("Mindmap not found with id: '{}'", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve mindmap with id: '{}'. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    private Mindmap buildMindmapFromRequest(MindmapCreateRequest request) {
        Mindmap mindmap = mapper.createRequestToEntity(request);

        if (mindmap.getNodes() != null) {
            mindmap.getNodes().forEach(node -> {
                if (node.getId() == null) {
                    node.setId(UUID.randomUUID().toString());
                }
            });
        }

        if (mindmap.getEdges() != null) {
            mindmap.getEdges().forEach(edge -> {
                if (edge.getId() == null) {
                    edge.setId(UUID.randomUUID().toString());
                }
            });
        }

        return mindmap;
    }

    private void updateMindmapFields(Mindmap existingMindmap, MindmapUpdateRequest request) {
        mapper.updateEntityFromRequest(request, existingMindmap);

        if (request.getNodes() != null) {
            List<MindmapNode> updatedNodes = mapper.nodeDtosToEntities(request.getNodes());

            updatedNodes.forEach(node -> {
                if (node.getId() == null) {
                    node.setId(UUID.randomUUID().toString());
                }
            });

            existingMindmap.setNodes(updatedNodes);
        }

        if (request.getEdges() != null) {
            var updatedEdges = mapper.edgeDtosToEntities(request.getEdges());

            updatedEdges.forEach(edge -> {
                if (edge.getId() == null) {
                    edge.setId(UUID.randomUUID().toString());
                }
            });

            existingMindmap.setEdges(updatedEdges);
        }
    }

    private Mindmap findMindmapById(String id) {
        return mindmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mindmap not found with id: " + id));
    }
}
