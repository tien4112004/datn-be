package com.datn.datnbe.student.mapper;

import org.springframework.stereotype.Component;

import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;

/**
 * Mapper for Student entity to/from DTOs.
 */
@Component
public class StudentEntityMapper {

    /**
     * Convert Student entity to response DTO.
     */
    public StudentResponseDto toResponseDto(Student entity) {
        if (entity == null) {
            return null;
        }

        return StudentResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .enrollmentDate(entity.getEnrollmentDate())
                .address(entity.getAddress())
                .parentContactEmail(entity.getParentContactEmail())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert StudentCreateRequest to Student entity.
     * Note: userId should be set separately after user creation.
     * This method is kept for reference but not used in the create flow.
     */
    public Student toEntity(StudentCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Student.builder()
                .address(request.getAddress())
                .parentContactEmail(request.getParentContactEmail())
                .build();
    }

    /**
     * Update entity fields from request DTO.
     */
    public void updateEntityFromRequest(Student entity, StudentUpdateRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getParentContactEmail() != null) {
            entity.setParentContactEmail(request.getParentContactEmail());
        }
    }
}
