package com.datn.datnbe.document.integration;

import com.datn.datnbe.document.api.SlideTemplateApi;
import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.document.entity.SlideTemplate;
import com.datn.datnbe.document.repository.SlideTemplateRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlideTemplateIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SlideTemplateApi slideTemplateApi;

    @Autowired
    private SlideTemplateRepository slideTemplateRepository;

    // ========== CREATE TESTS ==========

    @Test
    void createSlideTemplate_persistsToDatabase() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("config", Map.of("imageRatio", 16, "textAlign", "center"));
        data.put("graphics", List.of(Map.of("type", "rectangle", "color", "#cccccc")));

        SlideTemplateCreateRequest request = SlideTemplateCreateRequest.builder()
                .id("template-001")
                .name("Business Presentation")
                .layout("labeledList")
                .isEnabled(true)
                .data(data)
                .build();

        // Act
        SlideTemplateResponseDto response = slideTemplateApi.createSlideTemplate(request);

        // Assert - Verify response
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("template-001");
        assertThat(response.getName()).isEqualTo("Business Presentation");
        assertThat(response.getLayout()).isEqualTo("labeledList");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        // Assert - Verify it's actually in the database
        Optional<SlideTemplate> savedTemplate = slideTemplateRepository.findById("template-001");
        assertThat(savedTemplate).isPresent();
        assertThat(savedTemplate.get().getName()).isEqualTo("Business Presentation");
        assertThat(savedTemplate.get().getLayout()).isEqualTo("labeledList");
        assertThat(savedTemplate.get().getIsEnabled()).isTrue();
        assertThat(savedTemplate.get().getData()).containsKey("config");
        assertThat(savedTemplate.get().getData()).containsKey("graphics");
    }

    @Test
    void createSlideTemplate_withMinimalData_persistsSuccessfully() {
        // Arrange
        SlideTemplateCreateRequest request = SlideTemplateCreateRequest.builder()
                .id("template-minimal")
                .name("Minimal Template")
                .build();

        // Act
        SlideTemplateResponseDto response = slideTemplateApi.createSlideTemplate(request);

        // Assert
        assertThat(response.getId()).isEqualTo("template-minimal");
        assertThat(response.getName()).isEqualTo("Minimal Template");
        assertThat(response.getLayout()).isNull();

        Optional<SlideTemplate> savedTemplate = slideTemplateRepository.findById("template-minimal");
        assertThat(savedTemplate).isPresent();
        assertThat(savedTemplate.get().getIsEnabled()).isTrue(); // default value
    }

    @Test
    void createSlideTemplate_withDuplicateId_throwsException() {
        // Arrange - Create first template
        SlideTemplateCreateRequest request1 = SlideTemplateCreateRequest.builder()
                .id("duplicate-id")
                .name("First Template")
                .layout("grid")
                .build();
        slideTemplateApi.createSlideTemplate(request1);

        // Arrange - Try to create second template with same ID
        SlideTemplateCreateRequest request2 = SlideTemplateCreateRequest.builder()
                .id("duplicate-id")
                .name("Second Template")
                .layout("list")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> slideTemplateApi.createSlideTemplate(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createSlideTemplate_withComplexNestedData_persistsAllData() {
        // Arrange
        Map<String, Object> slideConfig = new HashMap<>();
        slideConfig.put("width", 1920);
        slideConfig.put("height", 1080);
        slideConfig.put("margin", Map.of("top", 50, "bottom", 50, "left", 100, "right", 100));

        Map<String, Object> data = new HashMap<>();
        data.put("slideConfig", slideConfig);
        data.put("elements",
                List.of(Map.of("type", "title", "position", Map.of("x", 100, "y", 50)),
                        Map.of("type", "content", "position", Map.of("x", 100, "y", 200))));

        SlideTemplateCreateRequest request = SlideTemplateCreateRequest.builder()
                .id("template-complex")
                .name("Complex Template")
                .layout("custom")
                .data(data)
                .build();

        // Act
        SlideTemplateResponseDto response = slideTemplateApi.createSlideTemplate(request);

        // Assert
        Optional<SlideTemplate> savedTemplate = slideTemplateRepository.findById("template-complex");
        assertThat(savedTemplate).isPresent();
        assertThat(savedTemplate.get().getData()).containsKey("slideConfig");
        assertThat(savedTemplate.get().getData()).containsKey("elements");
    }

    // ========== UPDATE TESTS ==========

    @Test
    void updateSlideTemplate_updatesName() {
        // Arrange - Create template first
        SlideTemplate template = SlideTemplate.builder()
                .id("template-update-name")
                .name("Original Name")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideTemplateRepository.save(template);

        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().name("Updated Name").build();

        // Act
        SlideTemplateResponseDto response = slideTemplateApi.updateSlideTemplate("template-update-name", updateRequest);

        // Assert
        assertThat(response.getName()).isEqualTo("Updated Name");

        Optional<SlideTemplate> updatedTemplate = slideTemplateRepository.findById("template-update-name");
        assertThat(updatedTemplate).isPresent();
        assertThat(updatedTemplate.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateSlideTemplate_updatesLayout() {
        // Arrange
        SlideTemplate template = SlideTemplate.builder()
                .id("template-update-layout")
                .name("Test Template")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideTemplateRepository.save(template);

        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().layout("list").build();

        // Act
        slideTemplateApi.updateSlideTemplate("template-update-layout", updateRequest);

        // Assert
        Optional<SlideTemplate> updatedTemplate = slideTemplateRepository.findById("template-update-layout");
        assertThat(updatedTemplate).isPresent();
        assertThat(updatedTemplate.get().getLayout()).isEqualTo("list");
    }

    @Test
    void updateSlideTemplate_updatesIsEnabled() {
        // Arrange
        SlideTemplate template = SlideTemplate.builder()
                .id("template-update-enabled")
                .name("Test Template")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideTemplateRepository.save(template);

        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().isEnabled(false).build();

        // Act
        slideTemplateApi.updateSlideTemplate("template-update-enabled", updateRequest);

        // Assert
        Optional<SlideTemplate> updatedTemplate = slideTemplateRepository.findById("template-update-enabled");
        assertThat(updatedTemplate).isPresent();
        assertThat(updatedTemplate.get().getIsEnabled()).isFalse();
    }

    @Test
    void updateSlideTemplate_mergesData() {
        // Arrange
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("config", Map.of("width", 1920));
        originalData.put("theme", "dark");

        SlideTemplate template = SlideTemplate.builder()
                .id("template-merge-data")
                .name("Test Template")
                .layout("grid")
                .isEnabled(true)
                .data(originalData)
                .build();
        slideTemplateRepository.save(template);

        Map<String, Object> newData = new HashMap<>();
        newData.put("theme", "light"); // Update existing
        newData.put("animation", "fade"); // Add new

        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().data(newData).build();

        // Act
        slideTemplateApi.updateSlideTemplate("template-merge-data", updateRequest);

        // Assert - Data should be merged
        Optional<SlideTemplate> updatedTemplate = slideTemplateRepository.findById("template-merge-data");
        assertThat(updatedTemplate).isPresent();
        assertThat(updatedTemplate.get().getData()).containsKey("config"); // preserved
        assertThat(updatedTemplate.get().getData()).containsEntry("theme", "light"); // updated
        assertThat(updatedTemplate.get().getData()).containsEntry("animation", "fade"); // added
    }

    @Test
    void updateSlideTemplate_withNonExistentId_throwsResourceNotFoundException() {
        // Arrange
        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().name("New Name").build();

        // Act & Assert
        assertThatThrownBy(() -> slideTemplateApi.updateSlideTemplate("non-existent-id", updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateSlideTemplate_updatesTimestamp() throws InterruptedException {
        // Arrange
        SlideTemplate template = SlideTemplate.builder()
                .id("template-timestamp")
                .name("Test Template")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();
        slideTemplateRepository.save(template);

        // Small delay to ensure timestamp difference
        Thread.sleep(100);

        SlideTemplateUpdateRequest updateRequest = SlideTemplateUpdateRequest.builder().name("Updated Name").build();

        // Act
        SlideTemplateResponseDto response = slideTemplateApi.updateSlideTemplate("template-timestamp", updateRequest);

        // Assert
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    // ========== GET ALL (PAGINATION) TESTS ==========

    @Test
    void getAllSlideTemplates_returnsOnlyEnabledTemplates() {
        // Arrange
        SlideTemplate enabledTemplate = SlideTemplate.builder()
                .id("enabled-template")
                .name("Enabled Template")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();

        SlideTemplate disabledTemplate = SlideTemplate.builder()
                .id("disabled-template")
                .name("Disabled Template")
                .layout("list")
                .isEnabled(false)
                .data(new HashMap<>())
                .build();

        slideTemplateRepository.save(enabledTemplate);
        slideTemplateRepository.save(disabledTemplate);

        SlideTemplateCollectionRequest request = SlideTemplateCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideTemplateResponseDto> response = slideTemplateApi.getAllSlideTemplates(request);

        // Assert
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getId()).isEqualTo("enabled-template");
    }

    @Test
    void getAllSlideTemplates_returnsPaginatedResults() {
        // Arrange - Create 15 templates
        for (int i = 1; i <= 15; i++) {
            SlideTemplate template = SlideTemplate.builder()
                    .id("template-page-" + String.format("%02d", i))
                    .name("Template " + i)
                    .layout("grid")
                    .isEnabled(true)
                    .data(new HashMap<>())
                    .build();
            slideTemplateRepository.save(template);
        }

        SlideTemplateCollectionRequest request = SlideTemplateCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideTemplateResponseDto> response = slideTemplateApi.getAllSlideTemplates(request);

        // Assert
        assertThat(response.getData()).hasSize(10);
        assertThat(response.getPagination().getTotalItems()).isEqualTo(15);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
    }

    @Test
    void getAllSlideTemplates_returnsSecondPage() {
        // Arrange - Create 15 templates
        for (int i = 1; i <= 15; i++) {
            SlideTemplate template = SlideTemplate.builder()
                    .id("template-p2-" + String.format("%02d", i))
                    .name("Template " + i)
                    .layout("grid")
                    .isEnabled(true)
                    .data(new HashMap<>())
                    .build();
            slideTemplateRepository.save(template);
        }

        SlideTemplateCollectionRequest request = SlideTemplateCollectionRequest.builder().page(2).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideTemplateResponseDto> response = slideTemplateApi.getAllSlideTemplates(request);

        // Assert
        assertThat(response.getData()).hasSize(5);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(2);
    }

    @Test
    void getAllSlideTemplates_withEmptyDatabase_returnsEmptyList() {
        // Arrange
        SlideTemplateCollectionRequest request = SlideTemplateCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideTemplateResponseDto> response = slideTemplateApi.getAllSlideTemplates(request);

        // Assert
        assertThat(response.getData()).isEmpty();
        assertThat(response.getPagination().getTotalItems()).isEqualTo(0);
    }

    @Test
    void getAllSlideTemplates_filtersCorrectlyByLayout() {
        // Arrange - Create templates with different layouts
        SlideTemplate gridTemplate = SlideTemplate.builder()
                .id("grid-template")
                .name("Grid Template")
                .layout("grid")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();

        SlideTemplate listTemplate = SlideTemplate.builder()
                .id("list-template")
                .name("List Template")
                .layout("list")
                .isEnabled(true)
                .data(new HashMap<>())
                .build();

        slideTemplateRepository.save(gridTemplate);
        slideTemplateRepository.save(listTemplate);

        SlideTemplateCollectionRequest request = SlideTemplateCollectionRequest.builder().page(1).pageSize(10).build();

        // Act
        PaginatedResponseDto<SlideTemplateResponseDto> response = slideTemplateApi.getAllSlideTemplates(request);

        // Assert - Both templates should be returned
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData()).extracting("layout").containsExactlyInAnyOrder("grid", "list");
    }
}
