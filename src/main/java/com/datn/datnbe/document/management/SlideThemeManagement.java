package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.document.entity.SlideTheme;
import com.datn.datnbe.document.mapper.SlideThemeMapper;
import com.datn.datnbe.document.repository.SlideThemeRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
}
