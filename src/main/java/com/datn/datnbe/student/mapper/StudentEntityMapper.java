package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;
import org.springframework.stereotype.Component;

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
                .fullName(entity.getFullName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .address(entity.getAddress())
                .parentName(entity.getParentName())
                .parentPhone(entity.getParentPhone())
                .classId(entity.getClassId())
                .enrollmentDate(entity.getEnrollmentDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update entity fields from request DTO.
     */
    public void updateEntityFromRequest(Student entity, StudentUpdateRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getFullName() != null) {
            entity.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            entity.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            entity.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getParentName() != null) {
            entity.setParentName(request.getParentName());
        }
        if (request.getParentPhone() != null) {
            entity.setParentPhone(request.getParentPhone());
        }
        if (request.getClassId() != null) {
            entity.setClassId(request.getClassId());
        }
        if (request.getEnrollmentDate() != null) {
            entity.setEnrollmentDate(request.getEnrollmentDate());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }
}
