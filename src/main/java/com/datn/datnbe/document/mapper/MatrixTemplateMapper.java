package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponseDto;
import com.datn.datnbe.document.entity.AssignmentMatrixEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for AssignmentMatrixEntity ↔ MatrixTemplateResponseDto conversion.
 * Uses Jackson ObjectMapper to parse matrixData JSON.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatrixTemplateMapper {

    private final ObjectMapper objectMapper;

    /**
     * Convert AssignmentMatrixEntity to MatrixTemplateResponseDto.
     * Parses matrixData JSON and calculates statistics.
     *
     * @param entity Matrix template entity
     * @return Response DTO with parsed matrix and statistics
     */
    public MatrixTemplateResponseDto toResponseDto(AssignmentMatrixEntity entity) {
        MatrixTemplateResponseDto.MatrixTemplateResponseDtoBuilder builder = MatrixTemplateResponseDto.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .subject(entity.getSubject())
                .grade(entity.getGrade())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Parse matrixData JSON
        try {
            AssignmentMatrixDto matrixDto = objectMapper.readValue(entity.getMatrixData(), AssignmentMatrixDto.class);

            // Calculate statistics using existing helper methods
            int totalQuestions = matrixDto.getTotalQuestions();
            int totalTopics = matrixDto.getDimensions() != null && matrixDto.getDimensions().getTopics() != null
                    ? matrixDto.getDimensions().getTopics().size()
                    : 0;

            builder.matrix(matrixDto).totalQuestions(totalQuestions).totalTopics(totalTopics);

        } catch (Exception e) {
            log.error("Failed to parse matrix data for entity id={}: {}", entity.getId(), e.getMessage());
            // Return DTO without matrix data on parse error
        }

        return builder.build();
    }
}
