package com.datn.datnbe.document.presentation;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.document.management.ImageManagement;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = ImageController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImageManagement imageManagement;

    private MediaResponseDto testMediaDto1;
    private MediaResponseDto testMediaDto2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testMediaDto1 = new MediaResponseDto();
        testMediaDto1.setId(1L);
        testMediaDto1.setOriginalFilename("test-image-1.jpg");
        testMediaDto1.setCdnUrl("https://cdn.example.com/test-image-1.jpg");
        testMediaDto1.setMediaType(MediaType.IMAGE);
        testMediaDto1.setFileSize(1024L);
        testMediaDto1.setCreatedAt(now);
        testMediaDto1.setUpdatedAt(now);

        testMediaDto2 = new MediaResponseDto();
        testMediaDto2.setId(2L);
        testMediaDto2.setOriginalFilename("test-image-2.png");
        testMediaDto2.setCdnUrl("https://cdn.example.com/test-image-2.png");
        testMediaDto2.setMediaType(MediaType.IMAGE);
        testMediaDto2.setFileSize(2048L);
        testMediaDto2.setCreatedAt(now);
        testMediaDto2.setUpdatedAt(now);
    }

    @Test
    void getImages_ShouldReturnPaginatedImages() throws Exception {
        // Given
        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(0)
                .pageSize(10)
                .totalItems(2L)
                .totalPages(1)
                .build();

        PaginatedResponseDto<MediaResponseDto> paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(List.of(testMediaDto1, testMediaDto2))
                .build();

        when(imageManagement.getImages(any(Pageable.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/images").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.totalItems").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].originalFilename").value("test-image-1.jpg"))
                .andExpect(jsonPath("$.data[0].url").value("https://cdn.example.com/test-image-1.jpg"))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    void getImages_WithoutPaginationParams_ShouldUseDefaultPagination() throws Exception {
        // Given
        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(0)
                .pageSize(20)
                .totalItems(1L)
                .totalPages(1)
                .build();

        PaginatedResponseDto<MediaResponseDto> paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(List.of(testMediaDto1))
                .build();

        when(imageManagement.getImages(any(Pageable.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/images")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getImages_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Given
        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(0)
                .pageSize(10)
                .totalItems(0L)
                .totalPages(0)
                .build();

        PaginatedResponseDto<MediaResponseDto> paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(List.of())
                .build();

        when(imageManagement.getImages(any(Pageable.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.pagination.totalItems").value(0));
    }

    @Test
    void getImageById_WithExistingId_ShouldReturnImage() throws Exception {
        // Given

        when(imageManagement.getImageById(1L)).thenReturn(testMediaDto1);

        // When & Then
        mockMvc.perform(get("/api/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.originalFilename").value("test-image-1.jpg"))
                .andExpect(jsonPath("$.data.url").value("https://cdn.example.com/test-image-1.jpg"))
                .andExpect(jsonPath("$.data.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.fileSize").value(1024));
    }

    @Test
    void getImageById_WithNonExistentId_ShouldThrowException() throws Exception {
        // Given
        when(imageManagement.getImageById(999L))
                .thenThrow(new AppException(ErrorCode.MEDIA_NOT_FOUND, "Image not found"));

        // When & Then
        mockMvc.perform(get("/api/images/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImages_WithCustomPageSize_ShouldRespectPageSize() throws Exception {
        // Given
        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(0)
                .pageSize(1)
                .totalItems(2L)
                .totalPages(2)
                .build();

        PaginatedResponseDto<MediaResponseDto> paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(List.of(testMediaDto1))
                .build();

        when(imageManagement.getImages(any(Pageable.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/images").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.pageSize").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getImages_WithSecondPage_ShouldReturnCorrectPage() throws Exception {
        // Given
        PaginationDto paginationDto = PaginationDto.builder()
                .currentPage(1)
                .pageSize(1)
                .totalItems(2L)
                .totalPages(2)
                .build();

        PaginatedResponseDto<MediaResponseDto> paginatedResponse = PaginatedResponseDto.<MediaResponseDto>builder()
                .pagination(paginationDto)
                .data(List.of(testMediaDto2))
                .build();

        when(imageManagement.getImages(any(Pageable.class))).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/images").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    void getImageById_WithDifferentImageId_ShouldReturnCorrectImage() throws Exception {
        // Given
        when(imageManagement.getImageById(2L)).thenReturn(testMediaDto2);

        // When & Then
        mockMvc.perform(get("/api/images/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.originalFilename").value("test-image-2.png"))
                .andExpect(jsonPath("$.data.fileSize").value(2048));
    }
}
