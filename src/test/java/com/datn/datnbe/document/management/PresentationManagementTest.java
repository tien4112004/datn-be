package com.datn.datnbe.document.management;

import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.request.PresentationCollectionRequest;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.enums.SlideElementType;
import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresentationManagementTest {

    @Mock
    private PresentationRepository presentationRepository;

    @Mock
    private PresentationEntityMapper mapper;

    @InjectMocks
    private PresentationManagement presentationService;

    private PresentationCreateRequest request;
    private SlideDto slideDto;
    private SlideDto.SlideElementDto elementDto;
    private SlideDto.SlideBackgroundDto backgroundDto;

    @BeforeEach
    void setUp() {
        backgroundDto = SlideDto.SlideBackgroundDto.builder().type("color").color("#ffffff").build();

        elementDto = SlideDto.SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("element-1")
                .left(100.0f)
                .top(200.0f)
                .width(300.0f)
                .height(50.0f)
                .content("Sample text content")
                .defaultFontName("Arial")
                .defaultColor("#000000")
                .build();

        slideDto = SlideDto.builder().id("slide-1").elements(List.of(elementDto)).background(backgroundDto).build();

        request = PresentationCreateRequest.builder().slides(List.of(slideDto)).build();
    }

    @Test
    void createPresentation_WithValidRequest_ShouldReturnResponseWithTitleAndSlides() {
        // Given
        Presentation mockEntity = Presentation.builder().id("test-id").title("Untitled Presentation").build();

        PresentationCreateResponseDto expectedResponse = PresentationCreateResponseDto.builder()
                .title("Untitled Presentation")
                .presentation(List.of(slideDto))
                .build();

        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(presentationRepository.save(any(Presentation.class))).thenReturn(mockEntity);
        when(mapper.toResponseDto(mockEntity)).thenReturn(expectedResponse);

        // When
        PresentationCreateResponseDto response = presentationService.createPresentation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Untitled Presentation");
        assertThat(response.getPresentation()).isNotNull();
        assertThat(response.getPresentation()).hasSize(1);
        assertThat(response.getPresentation().get(0)).isEqualTo(slideDto);
    }

    @Test
    void createPresentation_WithTitle_ShouldReturnResponseWithProvidedTitle() {
        // Given
        request.setTitle("My Custom Presentation");

        Presentation mockEntity = Presentation.builder().id("test-id").title("My Custom Presentation").build();

        PresentationCreateResponseDto expectedResponse = PresentationCreateResponseDto.builder()
                .title("My Custom Presentation")
                .presentation(List.of(slideDto))
                .build();

        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(presentationRepository.save(any(Presentation.class))).thenReturn(mockEntity);
        when(mapper.toResponseDto(mockEntity)).thenReturn(expectedResponse);

        // When
        PresentationCreateResponseDto response = presentationService.createPresentation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("My Custom Presentation");
        assertThat(response.getPresentation()).isNotNull();
        assertThat(response.getPresentation()).hasSize(1);
    }

    @Test
    void createPresentation_WithMultipleSlides_ShouldReturnAllSlides() {
        // Given
        SlideDto secondSlide = SlideDto.builder()
                .id("slide-2")
                .elements(List.of(elementDto))
                .background(backgroundDto)
                .build();

        request.setSlides(Arrays.asList(slideDto, secondSlide));

        Presentation mockEntity = Presentation.builder().id("test-id").title("Untitled Presentation").build();

        PresentationCreateResponseDto expectedResponse = PresentationCreateResponseDto.builder()
                .title("Untitled Presentation")
                .presentation(Arrays.asList(slideDto, secondSlide))
                .build();

        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(presentationRepository.save(any(Presentation.class))).thenReturn(mockEntity);
        when(mapper.toResponseDto(mockEntity)).thenReturn(expectedResponse);

        // When
        PresentationCreateResponseDto response = presentationService.createPresentation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Untitled Presentation");
        assertThat(response.getPresentation()).hasSize(2);
        assertThat(response.getPresentation().get(0).getId()).isEqualTo("slide-1");
        assertThat(response.getPresentation().get(1).getId()).isEqualTo("slide-2");
    }

    @Test
    void createPresentation_WithEmptySlides_ShouldReturnEmptyPresentation() {
        // Given
        request.setSlides(List.of());

        Presentation mockEntity = Presentation.builder().id("test-id").title("Untitled Presentation").build();

        PresentationCreateResponseDto expectedResponse = PresentationCreateResponseDto.builder()
                .title("Untitled Presentation")
                .presentation(List.of())
                .build();

        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(presentationRepository.save(any(Presentation.class))).thenReturn(mockEntity);
        when(mapper.toResponseDto(mockEntity)).thenReturn(expectedResponse);

        // When
        PresentationCreateResponseDto response = presentationService.createPresentation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Untitled Presentation");
        assertThat(response.getPresentation()).isEmpty();
    }

    @Test
    void createPresentation_WithComplexSlideElements_ShouldPreserveAllProperties() {
        SlideDto.SlideElementDto complexElement = SlideDto.SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("complex-element")
                .left(50.0f)
                .top(75.0f)
                .width(200.0f)
                .height(150.0f)
                .viewBox(Arrays.asList(0.0f, 0.0f, 100.0f, 100.0f))
                .path("M10,10 L90,90")
                .fill("#ff0000")
                .fixedRatio(true)
                .opacity(0.8f)
                .rotate(45.0f)
                .flipV(false)
                .lineHeight(1.5f)
                .start(Arrays.asList(10.0f, 20.0f))
                .end(Arrays.asList(90.0f, 80.0f))
                .points(Arrays.asList("10,10", "50,50", "90,90"))
                .color("#00ff00")
                .style("solid")
                .wordSpace(2.0f)
                .build();

        slideDto.setElements(Arrays.asList(elementDto, complexElement));

        Presentation mockEntity = Presentation.builder().id("test-id").title("Untitled Presentation").build();

        PresentationCreateResponseDto expectedResponse = PresentationCreateResponseDto.builder()
                .title("Untitled Presentation")
                .presentation(List.of(slideDto))
                .build();

        when(mapper.toEntity(request)).thenReturn(mockEntity);
        when(presentationRepository.save(any(Presentation.class))).thenReturn(mockEntity);
        when(mapper.toResponseDto(mockEntity)).thenReturn(expectedResponse);

        PresentationCreateResponseDto response = presentationService.createPresentation(request);

        assertThat(response).isNotNull();
        assertThat(response.getPresentation().get(0).getElements()).hasSize(2);

        SlideDto.SlideElementDto returnedComplexElement = response.getPresentation().get(0).getElements().get(1);
        assertThat(returnedComplexElement.getType()).isEqualTo(SlideElementType.TEXT);
        assertThat(returnedComplexElement.getId()).isEqualTo("complex-element");
        assertThat(returnedComplexElement.getViewBox()).containsExactly(0.0f, 0.0f, 100.0f, 100.0f);
        assertThat(returnedComplexElement.getPath()).isEqualTo("M10,10 L90,90");
        assertThat(returnedComplexElement.getFill()).isEqualTo("#ff0000");
        assertThat(returnedComplexElement.getFixedRatio()).isTrue();
        assertThat(returnedComplexElement.getOpacity()).isEqualTo(0.8f);
        assertThat(returnedComplexElement.getRotate()).isEqualTo(45.0f);
        assertThat(returnedComplexElement.getFlipV()).isFalse();
        assertThat(returnedComplexElement.getLineHeight()).isEqualTo(1.5f);
        assertThat(returnedComplexElement.getStart()).containsExactly(10.0f, 20.0f);
        assertThat(returnedComplexElement.getEnd()).containsExactly(90.0f, 80.0f);
        assertThat(returnedComplexElement.getPoints()).containsExactly("10,10", "50,50", "90,90");
        assertThat(returnedComplexElement.getColor()).isEqualTo("#00ff00");
        assertThat(returnedComplexElement.getStyle()).isEqualTo("solid");
        assertThat(returnedComplexElement.getWordSpace()).isEqualTo(2.0f);
    }

    @Test
    void getAllPresentations_WithExistingPresentations_ShouldReturnAllPresentations() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        Presentation presentation2 = Presentation.builder()
                .id("test-id-2")
                .title("Second Presentation")
                .createdAt(createdAt.plusHours(1))
                .updatedAt(createdAt.plusHours(1))
                .build();

        List<Presentation> presentations = Arrays.asList(presentation1, presentation2);

        PresentationListResponseDto responseDto1 = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        PresentationListResponseDto responseDto2 = PresentationListResponseDto.builder()
                .id("test-id-2")
                .title("Second Presentation")
                .createdAt(createdAt.plusHours(1))
                .updatedAt(createdAt.plusHours(1))
                .build();

        when(presentationRepository.findAll()).thenReturn(presentations);
        when(mapper.toListResponseDto(presentation1)).thenReturn(responseDto1);
        when(mapper.toListResponseDto(presentation2)).thenReturn(responseDto2);

        // When
        List<PresentationListResponseDto> result = presentationService.getAllPresentations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("test-id-1");
        assertThat(result.get(0).getTitle()).isEqualTo("First Presentation");
        assertThat(result.get(1).getId()).isEqualTo("test-id-2");
        assertThat(result.get(1).getTitle()).isEqualTo("Second Presentation");
    }

    @Test
    void getAllPresentations_WithNoPresentations_ShouldReturnEmptyList() {
        // Given
        when(presentationRepository.findAll()).thenReturn(List.of());

        // When
        List<PresentationListResponseDto> result = presentationService.getAllPresentations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getAllPresentations_WithCollectionRequest_ShouldReturnPaginatedResponse() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("asc")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .build();

        List<Presentation> presentations = List.of(presentation1);
        Page<Presentation> presentationPage = new PageImpl<>(presentations, PageRequest.of(0, 10), 1);

        PresentationListResponseDto responseDto = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .build();

        when(presentationRepository.findAll(any(Pageable.class))).thenReturn(presentationPage);
        when(mapper.toListResponseDto(presentation1)).thenReturn(responseDto);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getId()).isEqualTo("test-id-1");
        assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(result.getPagination().getPageSize()).isEqualTo(10);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
        assertThat(result.getPagination().isHasNextPage()).isFalse();
        assertThat(result.getPagination().isHasPreviousPage()).isFalse();
    }

    @Test
    void getAllPresentations_WithFilter_ShouldReturnFilteredResults() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("desc")
                .filter("Test")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .id("test-id-1")
                .title("Test Presentation")
                .createdAt(createdAt)
                .build();

        List<Presentation> presentations = List.of(presentation1);
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        PresentationListResponseDto responseDto = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("Test Presentation")
                .createdAt(createdAt)
                .build();

        when(presentationRepository.findByTitleContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(presentationPage);
        when(mapper.toListResponseDto(presentation1)).thenReturn(responseDto);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getTitle()).isEqualTo("Test Presentation");
    }

    @Test
    void getAllPresentations_WithEmptyFilter_ShouldReturnAllResults() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(5)
                .sort("asc")
                .filter("")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .build();

        List<Presentation> presentations = List.of(presentation1);
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt")), 1);

        PresentationListResponseDto responseDto = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("First Presentation")
                .createdAt(createdAt)
                .build();

        when(presentationRepository.findAll(any(Pageable.class))).thenReturn(presentationPage);
        when(mapper.toListResponseDto(presentation1)).thenReturn(responseDto);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getPagination().getPageSize()).isEqualTo(5);
    }

    @Test
    void getAllPresentations_WithDescSort_ShouldApplyCorrectSorting() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("desc")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .id("test-id-1")
                .title("Latest Presentation")
                .createdAt(createdAt)
                .build();

        List<Presentation> presentations = List.of(presentation1);
        Page<Presentation> presentationPage = new PageImpl<>(presentations,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        PresentationListResponseDto responseDto = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("Latest Presentation")
                .createdAt(createdAt)
                .build();

        when(presentationRepository.findAll(any(Pageable.class))).thenReturn(presentationPage);
        when(mapper.toListResponseDto(presentation1)).thenReturn(responseDto);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    void getAllPresentations_WithNoResultsFromFilter_ShouldReturnEmptyPaginatedResponse() {
        // Given
        PresentationCollectionRequest request = PresentationCollectionRequest.builder()
                .page(1)
                .pageSize(10)
                .sort("asc")
                .filter("NonExistentTitle")
                .build();

        Page<Presentation> emptyPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")), 0);

        when(presentationRepository.findByTitleContainingIgnoreCase(eq("NonExistentTitle"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        PaginatedResponseDto<PresentationListResponseDto> result = presentationService.getAllPresentations(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getPagination().getTotalItems()).isEqualTo(0);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(0);
    }
}