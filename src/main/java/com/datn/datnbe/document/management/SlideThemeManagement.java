package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideThemeCreateRequest;
import com.datn.datnbe.document.dto.request.SlideThemeUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.document.entity.SlideTheme;
import com.datn.datnbe.document.mapper.SlideThemeMapper;
import com.datn.datnbe.document.repository.SlideThemeRepository;
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
public class SlideThemeManagement implements SlideThemeApi {

    SlideThemeRepository slideThemeRepository;
    SlideThemeMapper slideThemeMapper;

    @Override
    public PaginatedResponseDto<SlideThemeResponseDto> getAllSlideThemes(SlideThemeCollectionRequest request) {
        log.info("Fetching slide themes - page: {}, pageSize: {}", request.getPage(), request.getPageSize());

        // Create pageable with sort by createdAt descending
        Pageable pageable = PageRequest
                .of(request.getPage() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // Query PostgreSQL for metadata and pagination info
        Page<SlideTheme> themePage = slideThemeRepository.findAllByIsEnabledTrue(pageable);

        // Map JPA entities (which now contain jsonb `data`) to response DTOs preserving order
        List<SlideThemeResponseDto> responseDtos = themePage.getContent().stream().map(theme -> {
            try {
                return slideThemeMapper.toResponseDto(theme);
            } catch (Exception ex) {
                log.warn("Failed to map SlideTheme to DTO for ID {}: {}", theme.getId(), ex.getMessage());
                return SlideThemeResponseDto.builder().id(theme.getId()).name(theme.getName()).build();
            }
        }).collect(Collectors.toList());

        // Build pagination info
        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                themePage.getTotalElements(), themePage.getTotalPages());

        log.info("Retrieved {} slide themes out of {} total", responseDtos.size(), themePage.getTotalElements());

        return new PaginatedResponseDto<>(responseDtos, pagination);
    }

    @Override
    @Transactional
    public SlideThemeResponseDto createSlideTheme(SlideThemeCreateRequest request) {
        log.info("Creating slide theme with id: {}", request.getId());

        // Check if theme with same ID already exists
        if (slideThemeRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Slide theme with ID '" + request.getId() + "' already exists");
        }

        SlideTheme entity = slideThemeMapper.toEntity(request);
        SlideTheme savedEntity = slideThemeRepository.save(entity);

        log.info("Created slide theme with id: {}", savedEntity.getId());
        return slideThemeMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public SlideThemeResponseDto updateSlideTheme(String id, SlideThemeUpdateRequest request) {
        log.info("Updating slide theme with id: {}", id);

        SlideTheme entity = slideThemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slide theme not found with id: " + id));

        slideThemeMapper.updateEntity(entity, request);
        SlideTheme savedEntity = slideThemeRepository.save(entity);

        log.info("Updated slide theme with id: {}", savedEntity.getId());
        return slideThemeMapper.toResponseDto(savedEntity);
    }
}
