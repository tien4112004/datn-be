package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.LessonApi;
import com.datn.datnbe.cms.dto.request.LessonCreateRequest;
import com.datn.datnbe.cms.dto.request.LessonUpdateRequest;
import com.datn.datnbe.cms.dto.response.LessonResponseDto;
import com.datn.datnbe.cms.entity.Lesson;
import com.datn.datnbe.cms.mapper.LessonMapper;
import com.datn.datnbe.cms.repository.LessonRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService implements LessonApi {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final SecurityContextUtils securityContextUtils;

    @Override
    @Transactional
    public LessonResponseDto createLesson(String classId, LessonCreateRequest request) {
        log.info("Creating lesson for class {}: {}", classId, request.getTitle());
        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setClassId(classId);
        lesson.setOwnerId(securityContextUtils.getCurrentUserId());
        lesson.setStatus(com.datn.datnbe.cms.enums.LessonStatus.DRAFT);
        Lesson saved = lessonRepository.save(lesson);
        return lessonMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<LessonResponseDto> getLessons(String classId, int page, int size, String search) {
        PageRequest pr = PageRequest.of(Math.max(0, page), size);
        Page<Lesson> p = lessonRepository.findAllWithFilters(classId, null, search, pr);
        PaginatedResponseDto<LessonResponseDto> resp = new PaginatedResponseDto<>();
        resp.setData(p.stream().map(lessonMapper::toResponseDto).collect(Collectors.toList()));
        resp.setPagination(PaginationDto.builder()
                .currentPage(p.getNumber() + 1)
                .pageSize(p.getSize())
                .totalItems(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .build());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponseDto getLessonById(String id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("Lesson with id '%s' not found", id)));
        return lessonMapper.toResponseDto(lesson);
    }

    @Override
    @Transactional
    public LessonResponseDto updateLesson(String id, LessonUpdateRequest request) {
        Lesson exist = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("Lesson with id '%s' not found", id)));
        lessonMapper.updateEntity(request, exist);
        Lesson saved = lessonRepository.save(exist);
        return lessonMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteLesson(String id) {
        lessonRepository.deleteById(id);
    }
}
