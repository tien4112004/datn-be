package com.datn.datnbe.document.management;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.response.PresentationDto;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.entity.valueobject.Slide;
import com.datn.datnbe.document.management.validation.PresentationValidation;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

@SpringBootTest(classes = {TestConfig.class, PresentationManagement.class,
        PresentationValidation.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetPresentationTest {

    @MockitoBean
    private PresentationRepository presentationRepository;

    @MockitoBean
    private ResourcePermissionApi resourcePermissionApi;

    @MockitoBean
    private com.datn.datnbe.sharedkernel.service.RustfsStorageService rustfsStorageService;

    @Autowired
    private com.datn.datnbe.document.mapper.PresentationEntityMapper presentationEntityMapper;

    @Autowired
    private PresentationValidation presentationValidation;

    @MockitoBean
    private com.datn.datnbe.document.service.DocumentVisitService documentVisitService;

    @Autowired
    private PresentationManagement presentationService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    
    private SecurityContext securityContext;
    
    private Authentication authentication;
    
    private Jwt jwt;

    private SlideDto slideDto;

    @BeforeEach
    void setUp() {
        // Setup security context mock
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        authentication = org.mockito.Mockito.mock(Authentication.class);
        jwt = org.mockito.Mockito.mock(Jwt.class);
        
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("test-user-id");
        
        Map<String, Object> background = new HashMap<>();
        background.put("type", "color");
        background.put("color", "#ffffff");

        Map<String, Object> element = new HashMap<>();
        element.put("type", "text");
        element.put("left", 100.0f);
        element.put("top", 200.0f);
        element.put("width", 300.0f);
        element.put("height", 50.0f);
        element.put("content", "Sample text content");
        element.put("defaultFontName", "Arial");
        element.put("defaultColor", "#000000");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(element));
        slideExtraFields.put("background", background);

        slideDto = SlideDto.builder().id("slide-1").extraFields(slideExtraFields).build();
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
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

        when(presentationRepository.findById(any(String.class))).thenReturn(Optional.of(presentation));

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

        when(presentationRepository.findById(any(String.class))).thenReturn(Optional.of(mockPresentation));

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

        when(presentationRepository.findById(any(String.class))).thenReturn(Optional.of(presentation));

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

        when(presentationRepository.findById(any(String.class))).thenReturn(Optional.of(mockPresentation));

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
