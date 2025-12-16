package com.datn.datnbe.student.mapper;

import com.datn.datnbe.student.dto.request.StudentCreateRequest;
import com.datn.datnbe.student.dto.request.StudentUpdateRequest;
import com.datn.datnbe.student.dto.response.StudentResponseDto;
import com.datn.datnbe.student.entity.Student;
import com.datn.datnbe.student.enums.Role;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert StudentCreateRequest to Student entity.
     */
    public Student toEntity(StudentCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Student.builder()
                .id(UUID.randomUUID().toString())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .role(Role.STUDENT)
                .status(request == null ? null : null) // Will use default from builder
                .build();
    }

    /**
     * Update entity fields from request DTO.
     */
    public void updateEntityFromRequest(Student entity, StudentUpdateRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getFirstName() != null) {
            entity.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            entity.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            entity.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarUrl() != null) {
            entity.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }
}
