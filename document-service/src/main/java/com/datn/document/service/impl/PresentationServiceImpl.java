package com.datn.document.service.impl;

import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.entity.Presentation;
import com.datn.document.mapper.PresentationEntityMapper;
import com.datn.document.repository.interfaces.PresentationRepository;
import com.datn.document.service.interfaces.PresentationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
