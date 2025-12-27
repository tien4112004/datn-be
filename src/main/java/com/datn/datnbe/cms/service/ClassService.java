package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.auth.entity.UserProfile;
import com.datn.datnbe.auth.repository.UserProfileRepo;
import com.datn.datnbe.cms.api.ClassApi;
import com.datn.datnbe.cms.dto.request.ClassCollectionRequest;
import com.datn.datnbe.cms.dto.request.ClassCreateRequest;
import com.datn.datnbe.cms.dto.request.ClassUpdateRequest;
import com.datn.datnbe.cms.dto.response.ClassListResponseDto;
import com.datn.datnbe.cms.dto.response.ClassResponseDto;
import com.datn.datnbe.cms.entity.ClassEntity;
import com.datn.datnbe.cms.mapper.ClassEntityMapper;
import com.datn.datnbe.cms.repository.ClassRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService implements ClassApi {

    private final ClassRepository classRepository;
    private final ClassEntityMapper classEntityMapper;
    private final SecurityContextUtils securityContextUtils;
    private final UserProfileRepo userProfileRepo;

    @Override
    @Transactional
    public ClassResponseDto createClass(ClassCreateRequest request) {
        log.info("Creating class with name: {}", request.getName());

        ClassEntity entity = classEntityMapper.toEntity(request);
        entity.setOwnerId(securityContextUtils.getCurrentUserId());
        ClassEntity savedEntity = classRepository.save(entity);

        log.info("Successfully created class with id: {}", savedEntity.getId());
        ClassResponseDto dto = classEntityMapper.toResponseDto(savedEntity);
        populateTeacherInfo(dto, savedEntity.getOwnerId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ClassListResponseDto> getAllClasses(ClassCollectionRequest request, String ownerId) {
        log.info("Fetching classes with filters: {}, for ownerId: {}", request, ownerId);

        Sort sort = buildSort(request.getSort());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);

        Page<ClassEntity> page = classRepository
                .findAllWithFilters(request.getSearch(), ownerId, request.getIsActive(), pageable);

        List<ClassListResponseDto> data = page.getContent().stream().map(entity -> {
            ClassListResponseDto dto = classEntityMapper.toListResponseDto(entity);
            populateTeacherInfo(dto, entity.getOwnerId());
            return dto;
        }).toList();

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(request.getPage())
                .pageSize(request.getPageSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return PaginatedResponseDto.<ClassListResponseDto>builder().data(data).pagination(pagination).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponseDto getClassById(String id) {
        log.info("Fetching class with id: {}", id);

        ClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND,
                        String.format("Class with id '%s' not found", id)));

        ClassResponseDto dto = classEntityMapper.toResponseDto(entity);
        populateTeacherInfo(dto, entity.getOwnerId());
        return dto;
    }

    @Override
    @Transactional
    public ClassResponseDto updateClass(String id, ClassUpdateRequest request) {
        log.info("Updating class with id: {}", id);

        ClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND,
                        String.format("Class with id '%s' not found", id)));

        classEntityMapper.updateEntity(request, entity);
        ClassEntity updatedEntity = classRepository.save(entity);

        log.info("Successfully updated class with id: {}", id);
        ClassResponseDto dto = classEntityMapper.toResponseDto(updatedEntity);
        populateTeacherInfo(dto, updatedEntity.getOwnerId());
        return dto;
    }

    private void populateTeacherInfo(ClassListResponseDto dto, String ownerId) {
        userProfileRepo.findByIdOrKeycloakUserId(ownerId).ifPresent(teacher -> {
            dto.setTeacher(mapToUserMinimalInfo(teacher));
        });
    }

    private void populateTeacherInfo(ClassResponseDto dto, String ownerId) {
        userProfileRepo.findByIdOrKeycloakUserId(ownerId).ifPresent(teacher -> {
            dto.setTeacher(mapToUserMinimalInfo(teacher));
        });
    }

    private UserMinimalInfoDto mapToUserMinimalInfo(UserProfile userProfile) {
        return UserMinimalInfoDto.builder()
                .id(userProfile.getId())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .email(userProfile.getEmail())
                .avatarUrl(userProfile.getAvatarUrl())
                .build();
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        String[] parts = sortParam.split("-");
        if (parts.length == 2) {
            String field = parts[0];
            Sort.Direction direction = "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(direction, mapSortField(field));
        }

        return Sort.by(Sort.Direction.ASC, "name");
    }

    private String mapSortField(String field) {
        return switch (field.toLowerCase()) {
            case "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            default -> "name";
        };
    }
}
