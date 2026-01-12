package com.datn.datnbe.document.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.dto.request.PresentationCollectionRequest;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.management.validation.PresentationValidation;
import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class GetAllPresentationsTest {

    @Mock
    private PresentationRepository presentationRepository;

    @Mock
    private ResourcePermissionApi resourcePermissionApi;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @Mock
    private com.datn.datnbe.sharedkernel.service.RustfsStorageService rustfsStorageService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    private PresentationEntityMapper mapper;

    private PresentationValidation validation;

    private PresentationManagement presentationService;

    private Presentation presentation1;
    private Presentation presentation2;
    private PresentationListResponseDto responseDto1;
    private PresentationListResponseDto responseDto2;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PresentationEntityMapper.class);
        presentationService = new PresentationManagement(presentationRepository, mapper, validation,
                resourcePermissionApi, rustfsStorageService);

        // Setup security context mock
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("test-user-id");

        // Mock resource permission API to return presentation IDs
        when(resourcePermissionApi.getAllResourceByTypeOfOwner(anyString(), eq("presentation")))
                .thenReturn(Arrays.asList("presentation-1", "presentation-2"));

        LocalDateTime now = LocalDateTime.now();

        presentation1 = Presentation.builder()
                .id("presentation-1")
                .title("First Presentation")
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(1))
                .build();

        presentation2 = Presentation.builder()
                .id("presentation-2")
                .title("Second Presentation")
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();

        responseDto1 = PresentationListResponseDto.builder()
                .id("presentation-1")
                .title("First Presentation")
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(1))
                .build();

        responseDto2 = PresentationListResponseDto.builder()
                .id("presentation-2")
                .title("Second Presentation")
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void getAllPresentations_WithPagination_ShouldReturnPaginatedResponse() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("asc")
                .build();

        List<Presentation> presentations = Arrays.asList(presentation1, presentation2);
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")), 2);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq(""), any(Pageable.class)))
                .thenReturn(presentationPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData()).containsExactly(responseDto1, responseDto2);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(result.getPagination().getPageSize()).isEqualTo(10);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(2L);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void getAllPresentations_WithFilter_ShouldReturnFilteredResults() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .filter("First")
                .sort("desc")
                .build();

        List<Presentation> filteredPresentations = List.of(presentation1);
        Page<Presentation> presentationPage = new PageImpl<>(filteredPresentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq("First"), any(Pageable.class)))
                .thenReturn(presentationPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0)).isEqualTo(responseDto1);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(1L);
    }

    @Test
    void getAllPresentations_WithSortDesc_ShouldSortByCreatedAtDesc() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("desc")
                .build();

        List<Presentation> presentations = Arrays.asList(presentation2, presentation1); // sorted desc by creation time
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 2);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq(""), any(Pageable.class)))
                .thenReturn(presentationPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData()).containsExactly(responseDto2, responseDto1); // desc order
    }

    @Test
    void getAllPresentations_WithEmptyFilter_ShouldReturnAllResults() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .filter("")
                .sort("asc")
                .build();

        List<Presentation> presentations = Arrays.asList(presentation1, presentation2);
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")), 2);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq(""), any(Pageable.class)))
                .thenReturn(presentationPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData()).containsExactly(responseDto1, responseDto2);
    }

    @Test
    void getAllPresentations_WithNoResults_ShouldReturnEmptyPaginatedResponse() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .filter("NonExistent")
                .sort("asc")
                .build();

        Page<Presentation> emptyPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")), 0);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq("NonExistent"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getPagination().getTotalItems()).isEqualTo(0L);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(0);
    }

    @Test
    void getAllPresentations_WithSecondPage_ShouldReturnCorrectPage() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(2)
                .pageSize(1)
                .sort("asc")
                .build();

        List<Presentation> secondPagePresentations = List.of(presentation2);
        Page<Presentation> presentationPage = new PageImpl<>(secondPagePresentations,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "createdAt")), 2);

        when(presentationRepository.findByIdInWithOptionalTitle(any(), eq(""), any(Pageable.class)))
                .thenReturn(presentationPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0)).isEqualTo(responseDto2);
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(result.getPagination().getPageSize()).isEqualTo(1);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(2L);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(2);
    }
}
