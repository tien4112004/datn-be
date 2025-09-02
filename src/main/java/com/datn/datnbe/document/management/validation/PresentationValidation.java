package com.datn.datnbe.document.management.validation;

import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresentationValidation {
    private final PresentationRepository presentationRepository;

    public void validatePresentationExists(Optional<Presentation> presentation, String id) {
        if (presentation.isEmpty()) {
            log.error("Presentation not found with ID: {}", id);
            throw new AppException(ErrorCode.PRESENTATION_NOT_FOUND, "Presentation with ID " + id + " does not exist.");
        }
    }

    public void validateTitleUniqueness(String title) {
        if (presentationRepository.existsByTitle(title)) {
            log.error("Presentation title must be unique: {}", title);
            throw new AppException(ErrorCode.PRESENTATION_TITLE_ALREADY_EXISTS);
        }
    }


}
