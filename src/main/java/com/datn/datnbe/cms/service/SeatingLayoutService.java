package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.SeatingLayoutApi;
import com.datn.datnbe.cms.dto.request.SeatingLayoutRequest;
import com.datn.datnbe.cms.dto.response.SeatingLayoutResponseDto;
import com.datn.datnbe.cms.entity.ClassEntity;
import com.datn.datnbe.cms.entity.SeatingLayout;
import com.datn.datnbe.cms.mapper.SeatingLayoutMapper;
import com.datn.datnbe.cms.repository.ClassRepository;
import com.datn.datnbe.cms.repository.SeatingLayoutRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatingLayoutService implements SeatingLayoutApi {

    private final SeatingLayoutRepository seatingLayoutRepository;
    private final ClassRepository classRepository;
    private final SeatingLayoutMapper seatingLayoutMapper;

    @Override
    @Transactional(readOnly = true)
    public SeatingLayoutResponseDto getSeatingChart(String classId) {
        log.info("Fetching seating chart for class: {}", classId);

        // Verify class exists
        if (!classRepository.existsById(classId)) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, String.format("Class with id '%s' not found", classId));
        }

        SeatingLayout seatingLayout = seatingLayoutRepository.findByClassId(classId).orElse(null);

        if (seatingLayout == null) {
            log.info("No seating chart found for class: {}, returning empty layout", classId);
            return null;
        }

        return seatingLayoutMapper.toLayoutResponseDto(seatingLayout);
    }

    @Override
    @Transactional
    public SeatingLayoutResponseDto saveSeatingChart(String classId, SeatingLayoutRequest request) {
        log.info("Saving seating chart for class: {}", classId);

        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND,
                        String.format("Class with id '%s' not found", classId)));

        SeatingLayout seatingLayout = seatingLayoutRepository.findByClassId(classId)
                .orElse(SeatingLayout.builder().classEntity(classEntity).build());

        seatingLayout.setLayoutConfig(seatingLayoutMapper.toLayoutConfig(request));
        SeatingLayout savedLayout = seatingLayoutRepository.save(seatingLayout);

        log.info("Successfully saved seating chart for class: {}", classId);
        return seatingLayoutMapper.toLayoutResponseDto(savedLayout);
    }
}
