package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresentationValidation {
    private final PresentationRepository presentationRepository;

    public void validatePresentationExists(Optional<Presentation> presentation, String id) {
        if (presentation.isEmpty()) {
            log.error("Presentation not found with ID: {}", id);
            throw new AppException(ErrorCode.PRESENTATION_NOT_FOUND, "Presentation not found with ID: " +id);
        }
    }

    public void validateTitleUniqueness(String title) {
        if (presentationRepository.existsByTitle(title)) {
            log.error("Presentation title must be unique: {}", title);
            throw new AppException(ErrorCode.PRESENTATION_TITLE_ALREADY_EXISTS);
        }
    }


}
