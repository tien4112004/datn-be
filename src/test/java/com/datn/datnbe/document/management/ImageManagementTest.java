package com.datn.datnbe.document.management;

import com.datn.datnbe.document.dto.response.MediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.document.enums.MediaType;
import com.datn.datnbe.document.mapper.MediaEntityMapper;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageManagementTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaEntityMapper mediaEntityMapper;

    @InjectMocks
    private ImageManagement imageManagement;

    private Media testMedia1;
    private Media testMedia2;
    private MediaResponseDto testMediaDto1;
    private MediaResponseDto testMediaDto2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testMedia1 = Media.builder()
                .id(1L)
                .originalFilename("test-image-1.jpg")
                .storageKey("storage/key/test-image-1.jpg")
                .cdnUrl("https://cdn.example.com/test-image-1.jpg")
                .mediaType(MediaType.IMAGE)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .createdAt(now)
                .updatedAt(now)
                .build();

        testMedia2 = Media.builder()
                .id(2L)
                .originalFilename("test-image-2.png")
                .storageKey("storage/key/test-image-2.png")
                .cdnUrl("https://cdn.example.com/test-image-2.png")
                .mediaType(MediaType.IMAGE)
                .fileSize(2048L)
                .contentType("image/png")
                .createdAt(now)
                .updatedAt(now)
                .build();

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
    void getImages_WithValidPageable_ShouldReturnPaginatedImages() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Media> mediaPage = new PageImpl<>(List.of(testMedia1, testMedia2), pageable, 2);

        when(mediaRepository.findByMediaType(MediaType.IMAGE, pageable)).thenReturn(mediaPage);
        when(mediaEntityMapper.toResponseDto(testMedia1)).thenReturn(testMediaDto1);
        when(mediaEntityMapper.toResponseDto(testMedia2)).thenReturn(testMediaDto2);

        // When
        PaginatedResponseDto<MediaResponseDto> result = imageManagement.getImages(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getFirst().getId()).isEqualTo(1L);
        assertThat(result.getData().getFirst().getOriginalFilename()).isEqualTo("test-image-1.jpg");
        assertThat(result.getData().getFirst().getCdnUrl()).isEqualTo("https://cdn.example.com/test-image-1.jpg");
    }

    @Test
    void getImages_WithEmptyResult_ShouldReturnEmptyPaginatedResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Media> mediaPage = new PageImpl<>(List.of(), pageable, 0);

        when(mediaRepository.findByMediaType(MediaType.IMAGE, pageable)).thenReturn(mediaPage);

        // When
        PaginatedResponseDto<MediaResponseDto> result = imageManagement.getImages(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
    }

    @Test
    void getImages_WithSecondPage_ShouldReturnCorrectPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);
        Page<Media> mediaPage = new PageImpl<>(List.of(testMedia2), pageable, 2);

        when(mediaRepository.findByMediaType(MediaType.IMAGE, pageable)).thenReturn(mediaPage);
        when(mediaEntityMapper.toResponseDto(testMedia2)).thenReturn(testMediaDto2);

        // When
        PaginatedResponseDto<MediaResponseDto> result = imageManagement.getImages(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getFirst().getId()).isEqualTo(2L);
    }

    @Test
    void getImageById_WithExistingId_ShouldReturnImage() {
        // Given
        Long imageId = 1L;
        when(mediaRepository.findById(imageId)).thenReturn(Optional.of(testMedia1));
        when(mediaEntityMapper.toResponseDto(testMedia1)).thenReturn(testMediaDto1);

        // When
        MediaResponseDto result = imageManagement.getImageById(imageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalFilename()).isEqualTo("test-image-1.jpg");
        assertThat(result.getCdnUrl()).isEqualTo("https://cdn.example.com/test-image-1.jpg");
        assertThat(result.getMediaType()).isEqualTo(MediaType.IMAGE);
        assertThat(result.getFileSize()).isEqualTo(1024L);
    }

    @Test
    void getImageById_WithNonExistentId_ShouldThrowMEDIA_NOT_FOUND() {
        // Given
        Long nonExistentId = 999L;
        when(mediaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> imageManagement.getImageById(nonExistentId)).isInstanceOf(AppException.class)
                .hasMessageContaining("Image not found")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.MEDIA_NOT_FOUND);
                });
    }

    @Test
    void getImageById_WithNullId_ShouldThrowException() {
        // Given
        Long nullId = null;

        // When & Then
        assertThatThrownBy(() -> imageManagement.getImageById(nullId)).isInstanceOf(Exception.class);
    }

    @Test
    void getImages_WithLargePageSize_ShouldReturnAllImages() {
        // Given
        Pageable pageable = PageRequest.of(0, 100);
        Page<Media> mediaPage = new PageImpl<>(List.of(testMedia1, testMedia2), pageable, 2);

        when(mediaRepository.findByMediaType(MediaType.IMAGE, pageable)).thenReturn(mediaPage);
        when(mediaEntityMapper.toResponseDto(testMedia1)).thenReturn(testMediaDto1);
        when(mediaEntityMapper.toResponseDto(testMedia2)).thenReturn(testMediaDto2);

        // When
        PaginatedResponseDto<MediaResponseDto> result = imageManagement.getImages(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
    }

    @Test
    void getImages_ShouldFilterByMediaTypeImage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Media> mediaPage = new PageImpl<>(List.of(testMedia1), pageable, 1);

        when(mediaRepository.findByMediaType(MediaType.IMAGE, pageable)).thenReturn(mediaPage);
        when(mediaEntityMapper.toResponseDto(testMedia1)).thenReturn(testMediaDto1);

        // When
        PaginatedResponseDto<MediaResponseDto> result = imageManagement.getImages(pageable);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void getImageById_WithDifferentImageFormats_ShouldReturnCorrectMetadata() {
        // Given
        Long imageId = 2L;
        when(mediaRepository.findById(imageId)).thenReturn(Optional.of(testMedia2));
        when(mediaEntityMapper.toResponseDto(testMedia2)).thenReturn(testMediaDto2);

        // When
        MediaResponseDto result = imageManagement.getImageById(imageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalFilename()).isEqualTo("test-image-2.png");
        assertThat(result.getFileSize()).isEqualTo(2048L);
    }
}
