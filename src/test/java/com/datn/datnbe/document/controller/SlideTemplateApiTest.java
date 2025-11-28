package com.datn.datnbe.document.controller;

import com.datn.datnbe.document.api.SlideTemplateApi;
import com.datn.datnbe.document.dto.request.SlideTemplateCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.SlideTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideTemplateResponseDto;
import com.datn.datnbe.document.presentation.SlideTemplateController;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SlideTemplateController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class SlideTemplateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlideTemplateApi slideTemplateApi;

    @Autowired
    private ObjectMapper objectMapper;

    private List<SlideTemplateResponseDto> mockTemplates;
    private PaginationDto mockPagination;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> template1Data = new HashMap<>();
        template1Data.put("config", Map.of("imageRatio", 16));
        template1Data.put("graphics", List.of(Map.of("type", "rectangle", "color", "#cccccc")));

        Map<String, Object> template2Data = new HashMap<>();
        template2Data.put("config", Map.of("imageRatio", 9));
        template2Data.put("graphics", List.of(Map.of("type", "circle", "color", "#ff0000")));

        SlideTemplateResponseDto template1 = SlideTemplateResponseDto.builder()
                .id("template-1")
                .name("Business Presentation")
                .layout("labeledList")
                .createdAt(now.minusDays(10))
                .updatedAt(now.minusDays(2))
                .additionalProperties(template1Data)
                .build();

        SlideTemplateResponseDto template2 = SlideTemplateResponseDto.builder()
                .id("template-2")
                .name("Creative Layout")
                .layout("grid")
                .createdAt(now.minusDays(7))
                .updatedAt(now.minusHours(3))
                .additionalProperties(template2Data)
                .build();

        mockTemplates = List.of(template1, template2);
        mockPagination = new PaginationDto(1, 10, 2L, 1);
    }

    @Test
    void getAllSlideTemplates_WithDefaultParameters_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        PaginatedResponseDto<SlideTemplateResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockTemplates,
                mockPagination);
        when(slideTemplateApi.getAllSlideTemplates(any(SlideTemplateCollectionRequest.class)))
                .thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-templates").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("template-1"))
                .andExpect(jsonPath("$.data[0].name").value("Business Presentation"))
                .andExpect(jsonPath("$.data[0].layout").value("labeledList"))
                .andExpect(jsonPath("$.data[0].config").exists())
                .andExpect(jsonPath("$.data[0].graphics").exists())
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists())
                .andExpect(jsonPath("$.data[1].id").value("template-2"))
                .andExpect(jsonPath("$.data[1].name").value("Creative Layout"))
                .andExpect(jsonPath("$.data[1].layout").value("grid"))
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void getAllSlideTemplates_WithCustomPageParameters_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        PaginationDto customPagination = new PaginationDto(3, 20, 50L, 3);
        PaginatedResponseDto<SlideTemplateResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockTemplates,
                customPagination);
        when(slideTemplateApi.getAllSlideTemplates(any(SlideTemplateCollectionRequest.class)))
                .thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-templates").param("page", "3")
                .param("pageSize", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.currentPage").value(3))
                .andExpect(jsonPath("$.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.pagination.totalItems").value(50))
                .andExpect(jsonPath("$.pagination.totalPages").value(3));
    }

    @Test
    void getAllSlideTemplates_WithEmptyResults_ShouldReturnEmptyPaginatedResponse() throws Exception {
        // Given
        PaginationDto emptyPagination = new PaginationDto(1, 10, 0L, 0);
        PaginatedResponseDto<SlideTemplateResponseDto> emptyResponse = new PaginatedResponseDto<>(List.of(),
                emptyPagination);
        when(slideTemplateApi.getAllSlideTemplates(any(SlideTemplateCollectionRequest.class)))
                .thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-templates").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0));
    }

    @Test
    void getAllSlideTemplates_WithInvalidPageParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/slide-templates").param("page", "-1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSlideTemplates_WithInvalidPageSizeParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/slide-templates").param("pageSize", "-5").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSlideTemplates_ShouldNotRequireAuthentication() throws Exception {
        // Given
        PaginatedResponseDto<SlideTemplateResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockTemplates,
                mockPagination);
        when(slideTemplateApi.getAllSlideTemplates(any(SlideTemplateCollectionRequest.class)))
                .thenReturn(paginatedResponse);

        // When & Then - No authentication headers, should still work
        mockMvc.perform(get("/api/slide-templates").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllSlideTemplates_WithLargePageSize_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        PaginationDto largePagination = new PaginationDto(1, 100, 100L, 1);
        PaginatedResponseDto<SlideTemplateResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockTemplates,
                largePagination);
        when(slideTemplateApi.getAllSlideTemplates(any(SlideTemplateCollectionRequest.class)))
                .thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-templates").param("pageSize", "100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.pageSize").value(100));
    }

    @Test
    void createSlideTemplate_ShouldReturnCreatedTemplate() throws Exception {
        // Given
        SlideTemplateCreateRequest request = SlideTemplateCreateRequest.builder()
                .id("new-template")
                .name("New Template")
                .layout("grid")
                .isEnabled(true)
                .data(Map.of("config", Map.of("imageRatio", 16)))
                .build();

        SlideTemplateResponseDto response = SlideTemplateResponseDto.builder()
                .id("new-template")
                .name("New Template")
                .layout("grid")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .additionalProperties(Map.of("config", Map.of("imageRatio", 16)))
                .build();

        when(slideTemplateApi.createSlideTemplate(any(SlideTemplateCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/slide-templates").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("new-template"))
                .andExpect(jsonPath("$.data.name").value("New Template"))
                .andExpect(jsonPath("$.data.layout").value("grid"));
    }

    @Test
    void createSlideTemplate_WithMissingId_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Template");
        request.put("layout", "grid");

        // When & Then
        mockMvc.perform(post("/api/slide-templates").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    void createSlideTemplate_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("id", "test-template");
        request.put("layout", "grid");

        // When & Then
        mockMvc.perform(post("/api/slide-templates").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    void updateSlideTemplate_ShouldReturnUpdatedTemplate() throws Exception {
        // Given
        SlideTemplateUpdateRequest request = SlideTemplateUpdateRequest.builder()
                .name("Updated Template Name")
                .layout("newLayout")
                .data(Map.of("config", Map.of("imageRatio", 9)))
                .build();

        SlideTemplateResponseDto response = SlideTemplateResponseDto.builder()
                .id("template-1")
                .name("Updated Template Name")
                .layout("newLayout")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now())
                .additionalProperties(Map.of("config", Map.of("imageRatio", 9)))
                .build();

        when(slideTemplateApi.updateSlideTemplate(eq("template-1"), any(SlideTemplateUpdateRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/slide-templates/template-1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("template-1"))
                .andExpect(jsonPath("$.data.name").value("Updated Template Name"))
                .andExpect(jsonPath("$.data.layout").value("newLayout"));
    }

    @Test
    void updateSlideTemplate_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        SlideTemplateUpdateRequest request = SlideTemplateUpdateRequest.builder().name("Updated Name").build();

        when(slideTemplateApi.updateSlideTemplate(eq("non-existent"), any(SlideTemplateUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Slide template not found"));

        // When & Then
        mockMvc.perform(put("/api/slide-templates/non-existent").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());
    }
}
