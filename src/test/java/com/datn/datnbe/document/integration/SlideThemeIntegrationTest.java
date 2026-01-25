package com.datn.datnbe.document.integration;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideThemeCreateRequest;
import com.datn.datnbe.document.dto.request.SlideThemeUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.document.entity.SlideTheme;
import com.datn.datnbe.document.repository.SlideThemeRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Disabled("Disabled - Docker required for integration tests")
class SlideThemeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SlideThemeApi slideThemeApi;

    @Autowired
    private SlideThemeRepository slideThemeRepository;

    // ========== CREATE TESTS ==========

    @Test
    void createSlideTheme_persistsToDatabase() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("backgroundColor", "#0066cc");
        data.put("fontColor", "#ffffff");
        data.put("fontName", "Arial");

        SlideThemeCreateRequest request = SlideThemeCreateRequest.builder()
                .id("theme-001")
                .name("Professional Blue")
                .isEnabled(true)
                .data(data)
                .build();

        // Act
        SlideThemeResponseDto response = slideThemeApi.createSlideTheme(request);

        // Assert - Verify response
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("theme-001");
        assertThat(response.getName()).isEqualTo("Professional Blue");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        // Assert - Verify it's actually in the database
        Optional<SlideTheme> savedTheme = slideThemeRepository.findById("theme-001");
        assertThat(savedTheme).isPresent();
        assertThat(savedTheme.get().getName()).isEqualTo("Professional Blue");
        assertThat(savedTheme.get().getIsEnabled()).isTrue();
        assertThat(savedTheme.get().getData()).containsEntry("backgroundColor", "#0066cc");
        assertThat(savedTheme.get().getData()).containsEntry("fontColor", "#ffffff");
        assertThat(savedTheme.get().getData()).containsEntry("fontName", "Arial");
    }

    @Test
    void createSlideTheme_withMinimalData_persistsSuccessfully() {
        // Arrange
        SlideThemeCreateRequest request = SlideThemeCreateRequest.builder()
                .id("theme-minimal")
                .name("Minimal Theme")
                .build();

        // Act
        SlideThemeResponseDto response = slideThemeApi.createSlideTheme(request);

        // Assert
        assertThat(response.getId()).isEqualTo("theme-minimal");
        assertThat(response.getName()).isEqualTo("Minimal Theme");

        Optional<SlideTheme> savedTheme = slideThemeRepository.findById("theme-minimal");
        assertThat(savedTheme).isPresent();
        assertThat(savedTheme.get().getIsEnabled()).isTrue(); // default value
    }

    @Test
    void createSlideTheme_withDuplicateId_throwsException() {
        // Arrange - Create first theme
        SlideThemeCreateRequest request1 = SlideThemeCreateRequest.builder()
                .id("duplicate-id")
                .name("First Theme")
                .build();
        slideThemeApi.createSlideTheme(request1);

        // Arrange - Try to create second theme with same ID
        SlideThemeCreateRequest request2 = SlideThemeCreateRequest.builder()
                .id("duplicate-id")
                .name("Second Theme")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> slideThemeApi.createSlideTheme(request2)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createSlideTheme_withComplexNestedData_persistsAllData() {
        // Arrange
        Map<String, Object> nestedConfig = new HashMap<>();
        nestedConfig.put("fontSize", 14);
        nestedConfig.put("lineHeight", 1.5);

        Map<String, Object> data = new HashMap<>();
        data.put("config", nestedConfig);
        data.put("colors", java.util.List.of("#ff0000", "#00ff00", "#0000ff"));

        SlideThemeCreateRequest request = SlideThemeCreateRequest.builder()
                .id("theme-complex")
                .name("Complex Theme")
                .data(data)
                .build();

        // Act
        SlideThemeResponseDto response = slideThemeApi.createSlideTheme(request);

        // Assert
        Optional<SlideTheme> savedTheme = slideThemeRepository.findById("theme-complex");
        assertThat(savedTheme).isPresent();
        assertThat(savedTheme.get().getData()).containsKey("config");
        assertThat(savedTheme.get().getData()).containsKey("colors");
    }

    // ========== UPDATE TESTS ==========

    @Test
    void updateSlideTheme_updatesName() {
        // Arrange - Create theme first
        SlideTheme theme = SlideTheme.builder()
                .id("theme-update-name")
                .name("Original Name")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideThemeRepository.save(theme);

        SlideThemeUpdateRequest updateRequest = SlideThemeUpdateRequest.builder().name("Updated Name").build();

        // Act
        SlideThemeResponseDto response = slideThemeApi.updateSlideTheme("theme-update-name", updateRequest);

        // Assert
        assertThat(response.getName()).isEqualTo("Updated Name");

        Optional<SlideTheme> updatedTheme = slideThemeRepository.findById("theme-update-name");
        assertThat(updatedTheme).isPresent();
        assertThat(updatedTheme.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateSlideTheme_updatesIsEnabled() {
        // Arrange
        SlideTheme theme = SlideTheme.builder()
                .id("theme-update-enabled")
                .name("Test Theme")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideThemeRepository.save(theme);

        SlideThemeUpdateRequest updateRequest = SlideThemeUpdateRequest.builder().isEnabled(false).build();

        // Act
        slideThemeApi.updateSlideTheme("theme-update-enabled", updateRequest);

        // Assert
        Optional<SlideTheme> updatedTheme = slideThemeRepository.findById("theme-update-enabled");
        assertThat(updatedTheme).isPresent();
        assertThat(updatedTheme.get().getIsEnabled()).isFalse();
    }

    @Test
    void updateSlideTheme_mergesData() {
        // Arrange
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("backgroundColor", "#ffffff");
        originalData.put("fontColor", "#000000");

        SlideTheme theme = SlideTheme.builder()
                .id("theme-merge-data")
                .name("Test Theme")
                .isEnabled(true)
                .data(originalData)
                .build();
        slideThemeRepository.save(theme);

        Map<String, Object> newData = new HashMap<>();
        newData.put("fontColor", "#333333"); // Update existing
        newData.put("fontSize", 16); // Add new

        SlideThemeUpdateRequest updateRequest = SlideThemeUpdateRequest.builder().data(newData).build();

        // Act
        slideThemeApi.updateSlideTheme("theme-merge-data", updateRequest);

        // Assert - Data should be merged
        Optional<SlideTheme> updatedTheme = slideThemeRepository.findById("theme-merge-data");
        assertThat(updatedTheme).isPresent();
        assertThat(updatedTheme.get().getData()).containsEntry("backgroundColor", "#ffffff"); // preserved
        assertThat(updatedTheme.get().getData()).containsEntry("fontColor", "#333333"); // updated
        assertThat(updatedTheme.get().getData()).containsEntry("fontSize", 16); // added
    }

    @Test
    void updateSlideTheme_withNonExistentId_throwsResourceNotFoundException() {
        // Arrange
        SlideThemeUpdateRequest updateRequest = SlideThemeUpdateRequest.builder().name("New Name").build();

        // Act & Assert
        assertThatThrownBy(() -> slideThemeApi.updateSlideTheme("non-existent-id", updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateSlideTheme_updatesTimestamp() throws InterruptedException {
        // Arrange
        SlideTheme theme = SlideTheme.builder()
                .id("theme-timestamp")
                .name("Test Theme")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideThemeRepository.save(theme);

        // Small delay to ensure timestamp difference
        Thread.sleep(100);

        SlideThemeUpdateRequest updateRequest = SlideThemeUpdateRequest.builder().name("Updated Name").build();

        // Act
        SlideThemeResponseDto response = slideThemeApi.updateSlideTheme("theme-timestamp", updateRequest);

        // Assert
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    // ========== GET ALL (PAGINATION) TESTS ==========

    @Test
    void getAllSlideThemes_returnsOnlyEnabledThemes() {
        // Arrange
        SlideTheme enabledTheme = SlideTheme.builder()
                .id("enabled-theme")
                .name("Enabled Theme")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();

        SlideTheme disabledTheme = SlideTheme.builder()
                .id("disabled-theme")
                .name("Disabled Theme")
                .isEnabled(false)
                .data(new HashMap<>())
                .build();

        slideThemeRepository.save(enabledTheme);
        slideThemeRepository.save(disabledTheme);

        SlideThemeCollectionRequest request = SlideThemeCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideThemeResponseDto> response = slideThemeApi.getAllSlideThemes(request);

        // Assert
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getId()).isEqualTo("enabled-theme");
    }

    @Test
    void getAllSlideThemes_returnsPaginatedResults() {
        // Arrange - Create 15 themes
        for (int i = 1; i <= 15; i++) {
            SlideTheme theme = SlideTheme.builder()
                    .id("theme-page-" + String.format("%02d", i))
                    .name("Theme " + i)
                    .isEnabled(true)
                    .data(new HashMap<>())
                    .build();
            slideThemeRepository.save(theme);
        }

        SlideThemeCollectionRequest request = SlideThemeCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideThemeResponseDto> response = slideThemeApi.getAllSlideThemes(request);

        // Assert
        assertThat(response.getData()).hasSize(10);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(15);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getAllSlideThemes_returnsSecondPage() {
        // Arrange - Create 15 themes
        for (int i = 1; i <= 15; i++) {
            SlideTheme theme = SlideTheme.builder()
                    .id("theme-p2-" + String.format("%02d", i))
                    .name("Theme " + i)
                    .isEnabled(true)
                    .data(new HashMap<>())
                    .build();
            slideThemeRepository.save(theme);
        }

        SlideThemeCollectionRequest request = SlideThemeCollectionRequest.builder().page(2).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideThemeResponseDto> response = slideThemeApi.getAllSlideThemes(request);

        // Assert
        assertThat(response.getData()).hasSize(5);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(2);
    }

    @Test
    void getAllSlideThemes_withEmptyDatabase_returnsEmptyList() {
        // Arrange
        SlideThemeCollectionRequest request = SlideThemeCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideThemeResponseDto> response = slideThemeApi.getAllSlideThemes(request);

        // Assert
        assertThat(response.getData()).isEmpty();
        assertThat(response.getPagination().getTotalItems()).isEqualTo(0);
    }
}
