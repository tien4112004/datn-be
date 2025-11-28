package com.datn.datnbe.document.controller;

import com.datn.datnbe.document.api.SlideThemeApi;
import com.datn.datnbe.document.dto.request.SlideThemeCollectionRequest;
import com.datn.datnbe.document.dto.request.SlideThemeCreateRequest;
import com.datn.datnbe.document.dto.request.SlideThemeUpdateRequest;
import com.datn.datnbe.document.dto.response.SlideThemeResponseDto;
import com.datn.datnbe.document.presentation.SlideThemeController;
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

@WebMvcTest(value = SlideThemeController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class SlideThemeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlideThemeApi slideThemeApi;

    @Autowired
    private ObjectMapper objectMapper;

    private List<SlideThemeResponseDto> mockThemes;
    private PaginationDto mockPagination;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> theme1Data = new HashMap<>();
        theme1Data.put("backgroundColor", "#0066cc");
        theme1Data.put("fontColor", "#ffffff");
        theme1Data.put("fontName", "Arial");

        Map<String, Object> theme2Data = new HashMap<>();
        theme2Data.put("backgroundColor", "#ff6600");
        theme2Data.put("fontColor", "#000000");
        theme2Data.put("fontName", "Roboto");

        SlideThemeResponseDto theme1 = SlideThemeResponseDto.builder()
                .id("theme-1")
                .name("Professional Blue")
                .createdAt(now.minusDays(5))
                .updatedAt(now.minusDays(1))
                .additionalProperties(theme1Data)
                .build();

        SlideThemeResponseDto theme2 = SlideThemeResponseDto.builder()
                .id("theme-2")
                .name("Vibrant Orange")
                .createdAt(now.minusDays(3))
                .updatedAt(now)
                .additionalProperties(theme2Data)
                .build();

        mockThemes = List.of(theme1, theme2);
        mockPagination = new PaginationDto(1, 10, 2L, 1);
    }

    @Test
    void getAllSlideThemes_WithDefaultParameters_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        PaginatedResponseDto<SlideThemeResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockThemes,
                mockPagination);
        when(slideThemeApi.getAllSlideThemes(any(SlideThemeCollectionRequest.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-themes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("theme-1"))
                .andExpect(jsonPath("$.data[0].name").value("Professional Blue"))
                .andExpect(jsonPath("$.data[0].backgroundColor").value("#0066cc"))
                .andExpect(jsonPath("$.data[0].fontColor").value("#ffffff"))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists())
                .andExpect(jsonPath("$.data[1].id").value("theme-2"))
                .andExpect(jsonPath("$.data[1].name").value("Vibrant Orange"))
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void getAllSlideThemes_WithCustomPageParameters_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        PaginationDto customPagination = new PaginationDto(2, 5, 15L, 3);
        PaginatedResponseDto<SlideThemeResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockThemes,
                customPagination);
        when(slideThemeApi.getAllSlideThemes(any(SlideThemeCollectionRequest.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-themes").param("page", "2")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.currentPage").value(2))
                .andExpect(jsonPath("$.pagination.pageSize").value(5))
                .andExpect(jsonPath("$.pagination.totalItems").value(15))
                .andExpect(jsonPath("$.pagination.totalPages").value(3));
    }

    @Test
    void getAllSlideThemes_WithEmptyResults_ShouldReturnEmptyPaginatedResponse() throws Exception {
        // Given
        PaginationDto emptyPagination = new PaginationDto(1, 10, 0L, 0);
        PaginatedResponseDto<SlideThemeResponseDto> emptyResponse = new PaginatedResponseDto<>(List.of(),
                emptyPagination);
        when(slideThemeApi.getAllSlideThemes(any(SlideThemeCollectionRequest.class))).thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/api/slide-themes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0));
    }

    @Test
    void getAllSlideThemes_WithInvalidPageParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/slide-themes").param("page", "0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSlideThemes_WithInvalidPageSizeParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/slide-themes").param("pageSize", "0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSlideThemes_ShouldNotRequireAuthentication() throws Exception {
        // Given
        PaginatedResponseDto<SlideThemeResponseDto> paginatedResponse = new PaginatedResponseDto<>(mockThemes,
                mockPagination);
        when(slideThemeApi.getAllSlideThemes(any(SlideThemeCollectionRequest.class))).thenReturn(paginatedResponse);

        // When & Then - No authentication headers, should still work
        mockMvc.perform(get("/api/slide-themes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createSlideTheme_ShouldReturnCreatedTheme() throws Exception {
        // Given
        SlideThemeCreateRequest request = SlideThemeCreateRequest.builder()
                .id("new-theme")
                .name("New Theme")
                .isEnabled(true)
                .data(Map.of("backgroundColor", "#ffffff"))
                .build();

        SlideThemeResponseDto response = SlideThemeResponseDto.builder()
                .id("new-theme")
                .name("New Theme")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .additionalProperties(Map.of("backgroundColor", "#ffffff"))
                .build();

        when(slideThemeApi.createSlideTheme(any(SlideThemeCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/slide-themes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("new-theme"))
                .andExpect(jsonPath("$.data.name").value("New Theme"));
    }

    @Test
    void createSlideTheme_WithMissingId_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Theme");

        // When & Then
        mockMvc.perform(post("/api/slide-themes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    void createSlideTheme_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("id", "test-theme");

        // When & Then
        mockMvc.perform(post("/api/slide-themes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    void updateSlideTheme_ShouldReturnUpdatedTheme() throws Exception {
        // Given
        SlideThemeUpdateRequest request = SlideThemeUpdateRequest.builder()
                .name("Updated Theme Name")
                .data(Map.of("backgroundColor", "#000000"))
                .build();

        SlideThemeResponseDto response = SlideThemeResponseDto.builder()
                .id("theme-1")
                .name("Updated Theme Name")
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now())
                .additionalProperties(Map.of("backgroundColor", "#000000"))
                .build();

        when(slideThemeApi.updateSlideTheme(eq("theme-1"), any(SlideThemeUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/slide-themes/theme-1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("theme-1"))
                .andExpect(jsonPath("$.data.name").value("Updated Theme Name"));
    }

    @Test
    void updateSlideTheme_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        SlideThemeUpdateRequest request = SlideThemeUpdateRequest.builder().name("Updated Name").build();

        when(slideThemeApi.updateSlideTheme(eq("non-existent"), any(SlideThemeUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Slide theme not found"));

        // When & Then
        mockMvc.perform(put("/api/slide-themes/non-existent").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());
    }
}
