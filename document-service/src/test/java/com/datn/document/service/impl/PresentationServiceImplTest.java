package com.datn.document.service.impl;

import com.datn.document.dto.SlideBackgroundDto;
import com.datn.document.dto.SlideDto;
import com.datn.document.dto.SlideElementDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.entity.Presentation;
import com.datn.document.enums.SlideElementType;
import com.datn.document.mapper.PresentationEntityMapper;
import com.datn.document.repository.interfaces.PresentationRepository;
import com.datn.document.service.impl.PresentationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresentationServiceImplTest {

    @Mock
    private PresentationRepository presentationRepository;

    @Mock
    private PresentationEntityMapper mapper;

    @InjectMocks
    private PresentationServiceImpl presentationService;

    private PresentationCreateRequest request;
    private SlideDto slideDto;
    private SlideElementDto elementDto;
    private SlideBackgroundDto backgroundDto;

    @BeforeEach
    void setUp() {
        backgroundDto = SlideBackgroundDto.builder()
                .type("color")
                .color("#ffffff")
                .build();

        elementDto = SlideElementDto.builder()
                .type(SlideElementType.CONTENT)
                .id("element-1")
                .left(100.0f)
                .top(200.0f)
                .width(300.0f)
                .height(50.0f)
                .content("Sample text content")
                .defaultFontName("Arial")
                .defaultColor("#000000")
                .build();

        slideDto = SlideDto.builder()
                .id("slide-1")
                .elements(List.of(elementDto))
                .background(backgroundDto)
                .build();

        request = PresentationCreateRequest.builder()
                .slides(List.of(slideDto))
                .build();
    }

    @Test
    void createPresentation_WithValidRequest_ShouldReturnResponseWithTitleAndSlides() {
        // Given
        Presentation mockEntity = Presentation.builder()
                .id("test-id")
                .title("Untitled Presentation")
                .build();

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

        Presentation mockEntity = Presentation.builder()
                .id("test-id")
                .title("My Custom Presentation")
                .build();

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

        Presentation mockEntity = Presentation.builder()
                .id("test-id")
                .title("Untitled Presentation")
                .build();

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

        Presentation mockEntity = Presentation.builder()
                .id("test-id")
                .title("Untitled Presentation")
                .build();

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
        SlideElementDto complexElement = SlideElementDto.builder()
                .type(SlideElementType.CONTENT)
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

        Presentation mockEntity = Presentation.builder()
                .id("test-id")
                .title("Untitled Presentation")
                .build();

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

        SlideElementDto returnedComplexElement = response.getPresentation().get(0).getElements().get(1);
        assertThat(returnedComplexElement.getType()).isEqualTo(SlideElementType.CONTENT);
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
}