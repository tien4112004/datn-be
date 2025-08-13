package com.datn.document.service.impl;

import com.datn.document.dto.common.PaginatedResponseDto;
import com.datn.document.dto.common.PaginationDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.request.PresentationUpdateRequest;
import com.datn.document.dto.request.PresentationUpdateTitleRequest;
import com.datn.document.dto.request.PresentationCollectionRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.dto.response.PresentationListResponseDto;
import com.datn.document.dto.response.PresentationUpdateResponseDto;
import com.datn.document.entity.Presentation;
import com.datn.document.exception.AppException;
import com.datn.document.exception.ErrorCode;
import com.datn.document.mapper.PresentationEntityMapper;
import com.datn.document.repository.PresentationRepository;
import com.datn.document.service.interfaces.PresentationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresentationServiceImpl implements PresentationService {

    private final PresentationRepository presentationRepository;
    private final PresentationEntityMapper mapper;

    @Override
    public PresentationCreateResponseDto createPresentation(PresentationCreateRequest request) {
        log.info("Creating presentation with title: {}", request.getTitle());

        Presentation presentation = mapper.toEntity(request);
        Presentation savedPresentation = presentationRepository.save(presentation);

        log.info("Presentation saved with ID: {}", savedPresentation.getId());
        return mapper.toResponseDto(savedPresentation);
    }

    @Override
    public List<PresentationListResponseDto> getAllPresentations() {
        List<Presentation> presentations = presentationRepository.findAll();

        return presentations.stream().map(mapper::toListResponseDto).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponseDto<PresentationListResponseDto> getAllPresentations(
            PresentationCollectionRequest request) {
        log.info("Fetching presentations with collection request - page: {}, pageSize: {}, filter: {}, sort: {}",
                request.getPage(),
                request.getPageSize(),
                request.getFilter(),
                request.getSort());

        // Create sort object
        Sort sortOrder = "desc".equalsIgnoreCase(request.getSort())
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");

        // Create pageable object
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sortOrder);

        // Fetch data based on filter
        Page<Presentation> presentationPage;
        if (StringUtils.hasText(request.getFilter())) {
            presentationPage = presentationRepository.findByTitleContainingIgnoreCase(request.getFilter(), pageable);
        } else {
            presentationPage = presentationRepository.findAll(pageable);
        }

        // Map to DTOs
        List<PresentationListResponseDto> presentations = presentationPage.getContent()
                .stream()
                .map(mapper::toListResponseDto)
                .collect(Collectors.toList());

        // Create pagination metadata
        PaginationDto pagination = new PaginationDto(request.getPage(), request.getPageSize(),
                presentationPage.getTotalElements(), presentationPage.getTotalPages(), presentationPage.hasNext(),
                presentationPage.hasPrevious());

        log.info("Retrieved {} presentations out of {} total",
                presentations.size(),
                presentationPage.getTotalElements());

        return new PaginatedResponseDto<>(presentations, pagination);
    }   

    @Override
    public void updatePresentation(String id, PresentationUpdateRequest request) {
        log.info("Updating presentation with ID: {} and title: {}", id, request.getTitle());

        Presentation existingPresentation = presentationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Presentation not found with ID: {}", id);
                    return new AppException(ErrorCode.PRESENTATION_NOT_FOUND);
                });

        if(presentationRepository.existsByTitle(request.getTitle())) {
            log.error("Presentation with title '{}' already exists", request.getTitle());
            throw new AppException(ErrorCode.PRESENTATION_TITLE_ALREADY_EXISTS);
        }


        mapper.updateEntity(request, existingPresentation);

        Presentation savedPresentation = presentationRepository.save(existingPresentation);

        log.info("Presentation updated with ID: {}", savedPresentation.getId());
    }

    @Override
    public void updateTitlePresentation(String id, PresentationUpdateTitleRequest request) {
        log.info("Updating title of presentation with ID: {} to title: {}", id, request.getTitle());
        Presentation existingPresentation = presentationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Presentation not found with ID: {}", id);
                    return new AppException(ErrorCode.PRESENTATION_NOT_FOUND);
                });
                
        if(presentationRepository.existsByTitle(request.getTitle())) {
            log.error("Presentation with title '{}' already exists", request.getTitle());
            throw new AppException(ErrorCode.PRESENTATION_TITLE_ALREADY_EXISTS);
        }

        existingPresentation.setTitle(request.getTitle());
        Presentation savedPresentation = presentationRepository.save(existingPresentation);
        log.info("Presentation title updated with ID: {}", savedPresentation.getId());
    }
}
