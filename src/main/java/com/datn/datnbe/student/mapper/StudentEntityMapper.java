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

    public StudentResponseDto toResponseDto(Student entity) {
        if (entity == null) {
            return null;
        }

        return StudentResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .enrollmentDate(entity.getEnrollmentDate())
                .gender(entity.getGender())
                .address(entity.getAddress())
                .parentContactEmail(entity.getParentContactEmail())
                .parentName(entity.getParentName())
                .parentPhone(entity.getParentPhone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Student toEntity(StudentCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Student.builder()
                .address(request.getAddress())
                .gender(request.getGender())
                .parentName(request.getParentName())
                .parentPhone(request.getParentPhone())
                .parentContactEmail(request.getParentContactEmail())
                .build();
    }

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

        if (request.getGender() != null) {
            entity.setGender(request.getGender());
        }

        if (request.getParentName() != null) {
            entity.setParentName(request.getParentName());
        }

        if (request.getParentPhone() != null) {
            entity.setParentPhone(request.getParentPhone());
        }
    }
}
