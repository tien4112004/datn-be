package com.datn.datnbe.document.management;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.document.entity.Mindmap;
import com.datn.datnbe.document.mapper.MindmapEntityMapper;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetMindmapTest {

    @Mock
    MindmapRepository mindmapRepository;

    @Mock
    MindmapEntityMapper mapper;

    @Mock
    ResourcePermissionApi resourcePermissionApi;

    @InjectMocks
    MindmapManagement management;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        // Setup security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("test-user-id");

        // Mock ResourcePermissionApi
        when(resourcePermissionApi.getAllResourceByTypeOfOwner(anyString(), eq("mindmap")))
                .thenReturn(List.of("1", "2", "3", "4", "5"));
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void getAllMindmaps_paginated_returnsPaginationMetadata() {
        // Arrange
        Mindmap entity = Mindmap.builder()
                .id("1")
                .title("t")
                .description("d")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        PageImpl<Mindmap> page = new PageImpl<>(List.of(entity),
                PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapListResponseDto dto = new MindmapListResponseDto();
        dto.setId("1");
        when(mapper.entityToListResponse(entity)).thenReturn(dto);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        var response = management.getAllMindmaps(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(11);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getAllMindmaps_paginated_firstPage_returnsCorrectMetadata() {
        // Arrange
        List<Mindmap> entities = createMindmapList(10);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(1, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(10);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(response.getPagination().getPageSize()).isEqualTo(10);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(25);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
    }

    @Test
    void getAllMindmaps_paginated_secondPage_returnsCorrectMetadata() {
        // Arrange
        List<Mindmap> entities = createMindmapList(10);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(1, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(10);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(response.getPagination().getPageSize()).isEqualTo(10);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(25);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
    }

    @Test
    void getAllMindmaps_paginated_lastPartialPage_returnsCorrectCount() {
        // Arrange
        List<Mindmap> entities = createMindmapList(5);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(2, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(2).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(5);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
    }

    @Test
    void getAllMindmaps_paginated_emptyResult_returnsEmptyData() {
        // Arrange
        PageImpl<Mindmap> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 10), 0);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).isEmpty();
        assertThat(response.getPagination().getTotalItems()).isEqualTo(0);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(0);
    }

    @Test
    void getAllMindmaps_paginated_sortsByCreatedAtDesc() {
        // Arrange
        List<Mindmap> entities = createMindmapList(10);
        PageRequest expectedPageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageImpl<Mindmap> page = new PageImpl<>(entities, expectedPageRequest, 10);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).sort("desc").build();

        // Act
        management.getAllMindmaps(request);

        // Assert
        verify(mindmapRepository).findByIdIn(any(), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void getAllMindmaps_paginated_largePageSize_handlesCorrectly() {
        // Arrange
        List<Mindmap> entities = createMindmapList(100);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(1, 100), 100);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(100).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(100);
        assertThat(response.getPagination().getPageSize()).isEqualTo(100);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
    }

    @Test
    void getAllMindmaps_paginated_singleItem_returnsCorrectMetadata() {
        // Arrange
        Mindmap entity = Mindmap.builder()
                .id("single")
                .title("Single Mindmap")
                .description("desc")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        PageImpl<Mindmap> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapListResponseDto dto = new MindmapListResponseDto();
        dto.setId("single");
        when(mapper.entityToListResponse(entity)).thenReturn(dto);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getAllMindmaps_paginated_verifyCorrectPageableParameters() {
        // Arrange
        PageImpl<Mindmap> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(3, 20), 0);
        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(3).pageSize(20).build();

        // Act
        management.getAllMindmaps(request);

        // Assert
        verify(mindmapRepository).findByIdIn(any(), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(20);
    }

    @Test
    void getAllMindmaps_paginated_mapsAllEntitiesCorrectly() {
        // Arrange
        List<Mindmap> entities = createMindmapList(3);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(1, 10), 3);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            Mindmap m = inv.getArgument(0);
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId(m.getId());
            dto.setTitle(m.getTitle());
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(response.getData().get(i).getId()).isEqualTo(entities.get(i).getId());
            assertThat(response.getData().get(i).getTitle()).isEqualTo(entities.get(i).getTitle());
        }
    }

    @Test
    void getAllMindmaps_paginated_calculatesCorrectTotalPages() {
        // Arrange - 25 total items, page size 10 should give 3 pages
        PageImpl<Mindmap> page = new PageImpl<>(createMindmapList(10), PageRequest.of(1, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId("id");
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(25);
    }

    @Test
    void getAllMindmaps_paginated_partialLastPage_calculatesCorrectly() {
        // Arrange - 25 total items, requesting page 2 (third page) with size 10
        // Should have 5 items on last page
        List<Mindmap> lastPageItems = createMindmapList(5);
        PageImpl<Mindmap> page = new PageImpl<>(lastPageItems, PageRequest.of(2, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            MindmapListResponseDto dto = new MindmapListResponseDto();
            dto.setId("id");
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(2).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(5);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
    }

    @Test
    void getAllMindmaps_paginated_singleItem_returnsOnePage() {
        // Arrange
        Mindmap entity = Mindmap.builder()
                .id("single")
                .title("Single Mindmap")
                .description("desc")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        PageImpl<Mindmap> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapListResponseDto dto = new MindmapListResponseDto();
        dto.setId("single");
        when(mapper.entityToListResponse(entity)).thenReturn(dto);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getAllMindmaps_paginated_largePageNumber_handlesCorrectly() {
        // Arrange - Request page 100 when only 3 pages exist
        PageImpl<Mindmap> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(100, 10), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(100).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert - Should return empty data but correct total metadata
        assertThat(response.getData()).isEmpty();
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(100);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(25);
    }

    @Test
    void getAllMindmaps_paginated_customPageSize_usesProvidedSize() {
        // Arrange
        List<Mindmap> entities = createMindmapList(5);
        PageImpl<Mindmap> page = new PageImpl<>(entities, PageRequest.of(1, 5), 25);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> {
            MindmapListResponseDto dto = new MindmapListResponseDto();
            return dto;
        });

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(5).build();

        // Act
        management.getAllMindmaps(request);

        // Assert - Verify the correct page size was used
        verify(mindmapRepository).findByIdIn(any(), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageSize()).isEqualTo(5);
    }

    @Test
    void getAllMindmaps_paginated_preservesPageNumberInResponse() {
        // Arrange
        PageImpl<Mindmap> page = new PageImpl<>(createMindmapList(10), PageRequest.of(5, 10), 100);

        when(mindmapRepository.findByIdIn(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.entityToListResponse(any(Mindmap.class))).thenAnswer(inv -> new MindmapListResponseDto());

        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(5).pageSize(10).build();

        // Act
        PaginatedResponseDto<MindmapListResponseDto> response = management.getAllMindmaps(request);

        // Assert
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(5);
    }

    @Test
    void getAllMindmaps_paginated_negativePageNumber_shouldThroError() {
        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(-1).pageSize(10).build();

        assertThatThrownBy(() -> management.getAllMindmaps(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getAllMindmaps_paginated_zeroPageSize_returnsEmptyData() {
        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(0).build();

        assertThatThrownBy(() -> management.getAllMindmaps(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getAllMindmaps_paginated_negativePageSize_throwsError() {
        // Arrange
        MindmapCollectionRequest request = MindmapCollectionRequest.builder().page(1).pageSize(-10).build();

        // Act & Assert
        assertThatThrownBy(() -> management.getAllMindmaps(request)).isInstanceOf(IllegalArgumentException.class);
    }

    private List<Mindmap> createMindmapList(int count) {
        List<Mindmap> mindmaps = new ArrayList<>();
        Instant nowInstant = Instant.now();
        for (int i = 0; i < count; i++) {
            Date createdDate = Date.from(nowInstant.minusSeconds((long)(count - i) * 86400));
            mindmaps.add(Mindmap.builder()
                    .id("id-" + i)
                    .title("Title " + i)
                    .description("Description " + i)
                    .createdAt(createdDate)
                    .updatedAt(new Date())
                    .build());
        }
        return mindmaps;
    }
}
