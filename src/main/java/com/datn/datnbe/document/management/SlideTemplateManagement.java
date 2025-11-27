package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.SlideTemplateApi;
import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.document.entity.SlideTemplate;
import com.datn.datnbe.document.mapper.SlideTemplateMapper;
import com.datn.datnbe.document.repository.SlideTemplateRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SlideTemplateManagement implements SlideTemplateApi {

    SlideTemplateRepository slideTemplateRepository;
    SlideTemplateMapper slideTemplateMapper;

    @Override
    public PaginatedResponseDto<SlideTemplateResponseDto> getAllSlideTemplates(SlideTemplateCollectionRequest request) {
        log.info("Fetching slide templates - page: {}, pageSize: {}", request.getPage(), request.getPageSize());

        // Create pageable with sort by createdAt descending
        Pageable pageable = PageRequest
                .of(request.getPage() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // Query PostgreSQL for metadata and pagination info
        Page<SlideTemplate> templatePage = slideTemplateRepository.findAllByIsEnabledTrue(pageable);

        // Map JPA entities (which now contain jsonb `data`) to response DTOs preserving order
        List<SlideTemplateResponseDto> responseDtos = templatePage.getContent().stream().map(template -> {
            try {
                return slideTemplateMapper.toResponseDto(template);
            } catch (Exception ex) {
                log.warn("Failed to map SlideTemplate to DTO for ID {}: {}", template.getId(), ex.getMessage());
                return SlideTemplateResponseDto.builder()
                        .id(template.getId())
                        .name(template.getName())
                        .layout(template.getLayout())
                        .build();
            }
        }).collect(Collectors.toList());

        // Build pagination info
        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                templatePage.getTotalElements(), templatePage.getTotalPages());

        log.info("Retrieved {} slide templates out of {} total", responseDtos.size(), templatePage.getTotalElements());

        return new PaginatedResponseDto<>(responseDtos, pagination);
    }

    @Override
    @Transactional
    public SlideTemplateResponseDto createSlideTemplate(SlideTemplateCreateRequest request) {
        log.info("Creating slide template with id: {}", request.getId());

        // Check if template with same ID already exists
        if (slideTemplateRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Slide template with ID '" + request.getId() + "' already exists");
        }

        SlideTemplate entity = slideTemplateMapper.toEntity(request);
        SlideTemplate savedEntity = slideTemplateRepository.save(entity);

        log.info("Created slide template with id: {}", savedEntity.getId());
        return slideTemplateMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public SlideTemplateResponseDto updateSlideTemplate(String id, SlideTemplateUpdateRequest request) {
        log.info("Updating slide template with id: {}", id);

        SlideTemplate entity = slideTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slide template not found with id: " + id));

        slideTemplateMapper.updateEntity(entity, request);
        SlideTemplate savedEntity = slideTemplateRepository.save(entity);

        log.info("Updated slide template with id: {}", savedEntity.getId());
        return slideTemplateMapper.toResponseDto(savedEntity);
    }
}
