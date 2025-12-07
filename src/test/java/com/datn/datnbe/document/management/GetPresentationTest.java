package com.datn.datnbe.document.management;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.entity.valueobject.Slide;
import com.datn.datnbe.document.management.validation.PresentationValidation;
import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

@SpringBootTest(classes = {TestConfig.class, PresentationManagement.class,
        PresentationValidation.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(MockitoExtension.class)
class GetPresentationTest {

    @MockitoBean
    private PresentationRepository presentationRepository;

    @MockitoBean
    private ResourcePermissionApi resourcePermissionApi;

    @Autowired
    private PresentationEntityMapper presentationEntityMapper;

    @Autowired
    private PresentationValidation presentationValidation;

    private PresentationManagement presentationService;

    private SlideDto slideDto;
    private SlideDto.SlideElementDto elementDto;
    private SlideDto.SlideBackgroundDto backgroundDto;

    @BeforeEach
    void setUp() {
        presentationService = new PresentationManagement(presentationRepository, presentationEntityMapper,
                presentationValidation, resourcePermissionApi);

        backgroundDto = SlideDto.SlideBackgroundDto.builder().type("color").color("#ffffff").build();

        elementDto = SlideDto.SlideElementDto.builder()
                .type("text")
                .left(100.0f)
                .top(200.0f)
                .width(300.0f)
                .height(50.0f)
                .content("Sample text content")
                .defaultFontName("Arial")
                .defaultColor("#000000")
                .build();

        slideDto = SlideDto.builder().id("slide-1").elements(List.of(elementDto)).background(backgroundDto).build();
    }

    @Test
    void getPresentation_WithExistingId_ShouldReturnPresentation() {
        // Given
        String presentationId = "68d1f7cb4828f3a0d4a432ec";
        LocalDateTime createdAt = LocalDateTime.now();

        Presentation presentation = Presentation.builder()
                .id(presentationId)
                .title("First Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(presentationRepository.findByIdActive(any(String.class))).thenReturn(Optional.of(presentation));

        // When
        PresentationDto result = presentationService.getPresentation(presentationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(presentationId);
        assertThat(result.getTitle()).isEqualTo("First Presentation");
    }

    @Test
    void getPresentation_WithNonExistentId_ShouldThrowPRESENTATION_NOT_FOUND() {
        // Given
        String nonExistentId = "507f1f77bcf86cd799439011"; // Valid ObjectId format but non-existent

        when(presentationRepository.findByIdActive(any(String.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> presentationService.getPresentation(nonExistentId)).isInstanceOf(AppException.class)
                .hasMessageContaining("Presentation not found with ID: " + nonExistentId)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRESENTATION_NOT_FOUND);
                });
    }

    @Test
    void getPresentation_WithPresentationContainingSlides_ShouldReturnCompleteDto() {
        // Given
        String presentationId = "68d1f7cb4828f3a0d4a432ec";
        LocalDateTime createdAt = LocalDateTime.now();

        Slide slideEntity = Slide.builder().id("slide-1").build();

        Presentation mockPresentation = Presentation.builder()
                .id(presentationId)
                .title("Presentation with Slides")
                .slides(List.of(slideEntity))
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(presentationRepository.findByIdActive(any(String.class))).thenReturn(Optional.of(mockPresentation));

        // When
        PresentationDto result = presentationService.getPresentation(presentationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(presentationId);
        assertThat(result.getTitle()).isEqualTo("Presentation with Slides");
        assertThat(result.getSlides()).hasSize(1);
        assertThat(result.getSlides().get(0).getId()).isEqualTo(slideDto.getId());
    }

    @Test
    void getPresentation_WithNullId_ShouldThrowIllegalArgumentException() {
        // Given
        String nullId = null;

        // When & Then
        assertThatThrownBy(() -> presentationService.getPresentation(nullId)).isInstanceOf(AppException.class)
                .hasMessageContaining("Presentation not found");
    }

    @Test
    void getPresentation_WithEmptyStringId_ShouldThrowIllegalArgumentException() {
        // Given
        String emptyId = "";

        // When & Then
        assertThatThrownBy(() -> presentationService.getPresentation(emptyId)).isInstanceOf(AppException.class)
                .hasMessageContaining("Presentation not found");
    }

    @Test
    void getPresentation_WithPresentationWithoutSlides_ShouldReturnDto() {
        // Given
        String presentationId = "68d1f7cb4828f3a0d4a432ec";
        LocalDateTime createdAt = LocalDateTime.now();

        Presentation presentation = Presentation.builder()
                .id(presentationId)
                .title("Empty Presentation")
                .slides(List.of())
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(presentationRepository.findByIdActive(any(String.class))).thenReturn(Optional.of(presentation));

        // When
        PresentationDto result = presentationService.getPresentation(presentationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(presentationId);
        assertThat(result.getTitle()).isEqualTo("Empty Presentation");
        assertThat(result.getSlides()).isEmpty();
    }

    @Test
    void getPresentation_WithMultipleSlides_ShouldReturnAllSlides() {
        // Given
        String presentationId = "68d1f7cb4828f3a0d4a432ec";
        LocalDateTime createdAt = LocalDateTime.now();

        Slide slideEntity1 = Slide.builder().id("slide-1").build();
        Slide slideEntity2 = Slide.builder().id("slide-2").build();

        Presentation mockPresentation = Presentation.builder()
                .id(presentationId)
                .title("Multi-slide Presentation")
                .slides(List.of(slideEntity1, slideEntity2))
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(presentationRepository.findByIdActive(any(String.class))).thenReturn(Optional.of(mockPresentation));

        // When
        PresentationDto result = presentationService.getPresentation(presentationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(presentationId);
        assertThat(result.getTitle()).isEqualTo("Multi-slide Presentation");
        assertThat(result.getSlides()).hasSize(2);
        assertThat(result.getSlides().get(0).getId()).isEqualTo("slide-1");
        assertThat(result.getSlides().get(1).getId()).isEqualTo("slide-2");
    }
}
