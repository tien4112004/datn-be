package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.SlidesApi;
import com.datn.datnbe.document.dto.request.*;
import com.datn.datnbe.document.management.validation.PresentationValidation;
import com.datn.datnbe.document.mapper.SlideEntityMapper;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.repository.PresentationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlidesManagement implements SlidesApi {

    private final PresentationRepository presentationRepository;
    private final SlideEntityMapper mapper;
    private final PresentationValidation validation;

    @Override
    public void upsertSlides(String id, SlidesUpsertRequest request) {
        log.info("Upserting slides for presentation with ID: {}, number of slides: {}",
                id,
                request.getSlides() != null ? request.getSlides().size() : 0);

        Optional<Presentation> presentation = presentationRepository.findById(id);
        validation.validatePresentationExists(presentation, id);
        Presentation existingPresentation = presentation.get();

        var slides = mapper.updateRequestToEntityList(request.getSlides());

        for (var upsertSlide : slides) {
            boolean removed = existingPresentation.getSlides()
                    .removeIf(slide -> slide.getId() != null && slide.getId().equals(upsertSlide.getId()));
            existingPresentation.getSlides().add(upsertSlide);
            if (removed) {
                log.info("Updated slide with ID: {}", upsertSlide.getId());
            } else {
                // Add new slide
                existingPresentation.getSlides().add(upsertSlide);
                log.info("Added new slide with ID: {}", upsertSlide.getId());
            }
        }

        Presentation savedPresentation = presentationRepository.save(existingPresentation);
        log.info("Slides upserted for presentation with ID: {}", savedPresentation.getId());
    }
}
