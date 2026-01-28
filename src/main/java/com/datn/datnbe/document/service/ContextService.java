package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.request.ContextCreateRequest;
import com.datn.datnbe.document.dto.request.ContextUpdateRequest;
import com.datn.datnbe.document.dto.response.ContextResponse;
import com.datn.datnbe.document.entity.Context;
import com.datn.datnbe.document.mapper.ContextMapper;
import com.datn.datnbe.document.repository.ContextRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

    private final ContextRepository contextRepository;
    private final ContextMapper contextMapper;

    /**
     * Create a new context
     */
    @Transactional
    public ContextResponse createContext(ContextCreateRequest request) {
        log.info("Creating new context with grade: {}", request.getGrade());

        Context context = contextMapper.toEntity(request);
        Context saved = contextRepository.save(context);

        log.info("Context created successfully with id: {}", saved.getId());
        return contextMapper.toDto(saved);
    }

    /**
     * Get context by id
     */
    @Transactional(readOnly = true)
    public ContextResponse getContextById(String contextId) {
        log.info("Fetching context with id: {}", contextId);

        Context context = contextRepository.findById(contextId).orElseThrow(() -> {
            log.warn("Context not found with id: {}", contextId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Context not found");
        });

        return contextMapper.toDto(context);
    }

    /**
     * Get all contexts with pagination
     */
    @Transactional(readOnly = true)
    public Page<ContextResponse> getAllContexts(int page, int size) {
        log.info("Fetching all contexts with page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        Page<Context> contexts = contextRepository.findAll(pageable);

        return contexts.map(contextMapper::toDto);
    }

    /**
     * Update context
     */
    @Transactional
    public ContextResponse updateContext(String contextId, ContextUpdateRequest request) {
        log.info("Updating context with id: {}", contextId);

        Context context = contextRepository.findById(contextId).orElseThrow(() -> {
            log.warn("Context not found with id: {}", contextId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Context not found");
        });

        contextMapper.updateEntity(context, request);
        Context updated = contextRepository.save(context);

        log.info("Context updated successfully with id: {}", updated.getId());
        return contextMapper.toDto(updated);
    }

    /**
     * Delete context
     */
    @Transactional
    public void deleteContext(String contextId) {
        log.info("Deleting context with id: {}", contextId);

        Context context = contextRepository.findById(contextId).orElseThrow(() -> {
            log.warn("Context not found with id: {}", contextId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Context not found");
        });

        contextRepository.delete(context);
        log.info("Context deleted successfully with id: {}", contextId);
    }
}
