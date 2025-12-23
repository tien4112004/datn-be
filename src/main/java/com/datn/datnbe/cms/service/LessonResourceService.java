package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.LessonResourceApi;
import com.datn.datnbe.cms.dto.request.LessonResourceCreateRequest;
import com.datn.datnbe.cms.dto.response.LessonResourceResponseDto;
import com.datn.datnbe.cms.entity.LessonResource;
import com.datn.datnbe.cms.mapper.LessonResourceMapper;
import com.datn.datnbe.cms.repository.LessonResourceRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonResourceService implements LessonResourceApi {

    private final LessonResourceRepository repository;
    private final LessonResourceMapper mapper;

    @Override
    @Transactional
    public LessonResourceResponseDto createResource(String lessonId, LessonResourceCreateRequest request) {
        LessonResource entity = mapper.toEntity(request);
        entity.setLessonId(lessonId);
        LessonResource saved = repository.save(entity);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResourceResponseDto> getResources(String lessonId) {
        return repository.findByLessonId(lessonId).stream().map(mapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResourceResponseDto getResourceById(String id) {
        LessonResource r = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("Resource with id '%s' not found", id)));
        return mapper.toResponseDto(r);
    }

    @Override
    @Transactional
    public LessonResourceResponseDto updateResource(String id, LessonResourceCreateRequest request) {
        LessonResource exist = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("Resource with id '%s' not found", id)));
        mapper.updateEntity(request, exist);
        LessonResource saved = repository.save(exist);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteResource(String id) {
        repository.deleteById(id);
    }
}
