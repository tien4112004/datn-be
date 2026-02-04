package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.request.ContextCollectionRequest;
import com.datn.datnbe.document.dto.request.ContextCreateRequest;
import com.datn.datnbe.document.dto.request.ContextsByIdsRequest;
import com.datn.datnbe.document.dto.request.ContextUpdateRequest;
import com.datn.datnbe.document.dto.response.ContextResponse;
import com.datn.datnbe.document.entity.Context;
import com.datn.datnbe.document.mapper.ContextMapper;
import com.datn.datnbe.document.repository.ContextRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

    private final ContextRepository contextRepository;
    private final ContextMapper contextMapper;

    /**
     * Get all contexts with filtering and pagination
     *
     * @param request       Collection request with filters
     * @param ownerIdFilter If not null, filter by owner (personal bank). If null,
     *                      filter where ownerId IS NULL (public bank)
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ContextResponse> getAllContexts(ContextCollectionRequest request,
            String ownerIdFilter) {

        log.info(
                "Fetching contexts - bankType: {}, page: {}, pageSize: {}, search: {}, grade: {}, subject: {}, ownerIdFilter: {}",
                request.getBankType(),
                request.getPage(),
                request.getPageSize(),
                request.getSearch(),
                request.getGrade(),
                request.getSubject(),
                ownerIdFilter);

        if (request.getPage() < 1) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page number must be >= 1");
        }
        if (request.getPageSize() < 1 || request.getPageSize() > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must be between 1 and 100");
        }

        int pageIndex = request.getPage() - 1;

        Sort.Direction direction = Sort.Direction.DESC;
        if (request.getSortDirection() != null) {
            direction = request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize(), direction, sortBy);

        Specification<Context> spec = buildSpecification(request, ownerIdFilter);
        Page<Context> contextPage = contextRepository.findAll(spec, pageable);

        log.debug("Fetched {} contexts", contextPage.getSize());

        List<ContextResponse> dtos = contextPage.getContent()
                .stream()
                .map(contextMapper::toDto)
                .collect(Collectors.toList());

        PaginationDto paginationInfo = PaginationDto.builder()
                .currentPage(request.getPage())
                .pageSize(contextPage.getSize())
                .totalPages(contextPage.getTotalPages())
                .totalItems(contextPage.getTotalElements())
                .build();

        return PaginatedResponseDto.<ContextResponse>builder().data(dtos).pagination(paginationInfo).build();
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
     * Get multiple contexts by their IDs
     * Preserves order from request, silently skips missing IDs
     */
    @Transactional(readOnly = true)
    public List<ContextResponse> getContextsByIds(ContextsByIdsRequest request) {
        log.info("Fetching contexts by IDs: {}", request.getIds());

        List<Context> contexts = contextRepository.findByIdIn(request.getIds());

        // Create a map for O(1) lookup
        Map<String, Context> contextMap = contexts.stream().collect(Collectors.toMap(Context::getId, c -> c));

        // Preserve order from request - return in same order as IDs provided
        List<ContextResponse> orderedContexts = request.getIds()
                .stream()
                .map(contextMap::get)
                .filter(Objects::nonNull)
                .map(contextMapper::toDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} contexts out of {} requested IDs", orderedContexts.size(), request.getIds().size());

        return orderedContexts;
    }

    /**
     * Create a new context
     *
     * @param request Create request
     * @param ownerId Owner ID (null for public context, userId for personal
     *                context)
     */
    @Transactional
    public ContextResponse createContext(ContextCreateRequest request, String ownerId) {
        log.info("Creating new context with grade: {}, ownerId: {}", request.getGrade(), ownerId);

        Context context = contextMapper.toEntity(request);
        context.setOwnerId(ownerId);

        Context saved = contextRepository.save(context);

        log.info("Context created successfully with id: {}, ownerId: {}", saved.getId(), ownerId);
        return contextMapper.toDto(saved);
    }

    /**
     * Update context
     *
     * @param contextId Context ID
     * @param request   Update request
     * @param userId    Current user ID (null for admin operations on public
     *                  contexts)
     */
    @Transactional
    public ContextResponse updateContext(String contextId, ContextUpdateRequest request, String userId) {
        log.info("Updating context with id: {}, userId: {}", contextId, userId);

        Context context = contextRepository.findById(contextId).orElseThrow(() -> {
            log.warn("Context not found with id: {}", contextId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Context not found");
        });

        verifyOwnership(context, userId, contextId);

        contextMapper.updateEntity(context, request);
        Context updated = contextRepository.save(context);

        log.info("Context updated successfully with id: {}", updated.getId());
        return contextMapper.toDto(updated);
    }

    /**
     * Delete context
     *
     * @param contextId Context ID
     * @param userId    Current user ID (null for admin operations on public
     *                  contexts)
     */
    @Transactional
    public void deleteContext(String contextId, String userId) {
        log.info("Deleting context with id: {}, userId: {}", contextId, userId);

        Context context = contextRepository.findById(contextId).orElseThrow(() -> {
            log.warn("Context not found with id: {}", contextId);
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Context not found");
        });

        verifyOwnership(context, userId, contextId);

        contextRepository.delete(context);
        log.info("Context deleted successfully with id: {}", contextId);
    }

    /**
     * Verify that the user has permission to modify the context.
     * For personal contexts (ownerId != null): user must be the owner
     * For public contexts (ownerId == null): only admin can modify (userId will be
     * null from admin controller)
     */
    private void verifyOwnership(Context context, String userId, String contextId) {
        // If userId is null, it's an admin operation - only allow for public contexts
        if (userId == null) {
            if (context.getOwnerId() != null) {
                log.warn("Admin attempted to modify personal context {} owned by {}", contextId, context.getOwnerId());
                throw new AppException(ErrorCode.FORBIDDEN, "Admins can only modify public contexts");
            }
            return;
        }

        // For regular users, they can only modify their own personal contexts
        if (context.getOwnerId() == null || !context.getOwnerId().equals(userId)) {
            log.warn("User {} attempted to modify context {} owned by {}", userId, contextId, context.getOwnerId());
            throw new AppException(ErrorCode.FORBIDDEN, "You do not have permission to modify this context");
        }
    }

    private Specification<Context> buildSpecification(ContextCollectionRequest request, String ownerIdFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Owner filter - key logic for personal/public
            if (ownerIdFilter != null) {
                // Personal bank - filter by owner
                predicates.add(cb.equal(root.get("ownerId"), ownerIdFilter));
            } else {
                // Public bank - only contexts with null ownerId
                predicates.add(cb.isNull(root.get("ownerId")));
            }

            // Search by title, content, or author
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String searchLower = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("title")), searchLower),
                        cb.like(cb.lower(root.get("content")), searchLower),
                        cb.like(cb.lower(root.get("author")), searchLower)));
            }

            // Filter by grade (supports multi-select)
            if (request.getGrade() != null && !request.getGrade().isEmpty()) {
                predicates.add(root.get("grade").in(request.getGrade()));
            }

            // Filter by subject (supports multi-select)
            if (request.getSubject() != null && !request.getSubject().isEmpty()) {
                predicates.add(root.get("subject").in(request.getSubject()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
