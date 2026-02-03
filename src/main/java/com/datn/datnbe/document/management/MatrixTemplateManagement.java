package com.datn.datnbe.document.management;

import com.datn.datnbe.document.api.MatrixTemplateApi;
import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponse;
import com.datn.datnbe.document.entity.MatrixTemplate;
import com.datn.datnbe.document.mapper.MatrixTemplateMapper;
import com.datn.datnbe.document.repository.MatrixTemplateRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatrixTemplateManagement implements MatrixTemplateApi {

    private final MatrixTemplateRepository matrixTemplateRepository;
    private final MatrixTemplateMapper matrixTemplateMapper;

    @Override
    @Transactional
    public MatrixTemplateResponse createMatrixTemplate(MatrixTemplateCreateRequest request) {
        log.info("Creating matrix template with title: {}", request.getTitle());

        MatrixTemplate entity = matrixTemplateMapper.toEntity(request);
        MatrixTemplate saved = matrixTemplateRepository.save(entity);

        log.info("Created matrix template with id: {}", saved.getId());
        return matrixTemplateMapper.toDto(saved);
    }

    @Override
    public PaginatedResponseDto<MatrixTemplateResponse> getMatrixTemplates(int page, int size) {
        log.info("Fetching matrix templates - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MatrixTemplate> pageResult = matrixTemplateRepository.findAll(pageable);

        var data = pageResult.getContent().stream().map(matrixTemplateMapper::toDto).collect(Collectors.toList());

        var pagination = PaginationDto.builder()
                .currentPage(page)
                .pageSize(size)
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();

        return PaginatedResponseDto.<MatrixTemplateResponse>builder().data(data).pagination(pagination).build();
    }

    @Override
    public MatrixTemplateResponse getMatrixTemplateById(String id) {
        log.info("Fetching matrix template with id: {}", id);

        MatrixTemplate entity = matrixTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        return matrixTemplateMapper.toDto(entity);
    }

    @Override
    @Transactional
    public MatrixTemplateResponse updateMatrixTemplate(String id, MatrixTemplateUpdateRequest request) {
        log.info("Updating matrix template with id: {}", id);

        MatrixTemplate entity = matrixTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        matrixTemplateMapper.updateEntity(entity, request);
        MatrixTemplate saved = matrixTemplateRepository.save(entity);

        log.info("Updated matrix template with id: {}", saved.getId());
        return matrixTemplateMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteMatrixTemplate(String id) {
        log.info("Deleting matrix template with id: {}", id);

        MatrixTemplate entity = matrixTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrix template not found with id: " + id));

        matrixTemplateRepository.delete(entity);
        log.info("Deleted matrix template with id: {}", id);
    }
}
