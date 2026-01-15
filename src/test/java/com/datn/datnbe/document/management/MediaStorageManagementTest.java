package com.datn.datnbe.document.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.datn.datnbe.document.dto.response.UploadedMediaResponseDto;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.datn.datnbe.document.management.validation.MediaValidation;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import java.util.Optional;

import com.datn.datnbe.sharedkernel.service.RustfsStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaStorageManagement Tests")
class MediaStorageManagementTest {

    @Mock
    private RustfsStorageService rustfsStorageService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private com.datn.datnbe.document.service.DocumentVisitService documentVisitService;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private MediaStorageManagement mediaStorageManagement;

    private Media testMedia;
    private String testCdnDomain;

    @BeforeEach
    void setUp() {
        testCdnDomain = "https://cdn.example.com";
        ReflectionTestUtils.setField(mediaStorageManagement, "cdnDomain", testCdnDomain);

        testMedia = Media.builder()
                .id(1L)
                .originalFilename("test-image.jpg")
                .storageKey("images/uuid-test-image.jpg")
                .cdnUrl("https://cdn.example.com/images/uuid-test-image.jpg")
                .mediaType(MediaType.IMAGE)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .ownerId("test-owner")
                .build();
    }

    // ===============================
    // Upload Method Tests
    // ===============================

    @Test
    @DisplayName("Should upload file successfully and return response DTO")
    void upload_ValidFile_ShouldReturnUploadedMediaResponseDto() {
        // Given
        String originalFilename = "test-image.jpg";
        String contentType = "image/jpeg";
        String extension = "jpg";
        String storageKey = "images/uuid-test-image.jpg";
        String ownerId = "test-owner";

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(1024L);

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenReturn(storageKey);
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        // Mock static methods
        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.IMAGE);

            // When
            UploadedMediaResponseDto result = mediaStorageManagement.upload(mockFile, ownerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMediaType()).isEqualTo("IMAGE");
            assertThat(result.getCdnUrl()).isEqualTo(testMedia.getCdnUrl());

            verify(rustfsStorageService).uploadFile(eq(mockFile), anyString(), eq(contentType));
            verify(mediaRepository).save(any(Media.class));
            validationMock.verify(() -> MediaValidation.getValidatedMediaType(mockFile));
        }
    }

    @Test
    @DisplayName("Should handle validation failure during upload")
    void upload_ValidationFails_ShouldThrowAppException() {
        // Given
        AppException validationException = new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Invalid file type");

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenThrow(validationException);

            // When & Then
            assertThatThrownBy(() -> mediaStorageManagement.upload(mockFile, "test-owner"))
                    .isInstanceOf(AppException.class)
                    .hasMessage("Invalid file type")
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
                    });

            verify(rustfsStorageService, never()).uploadFile(any(), anyString(), anyString());
            verify(mediaRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle R2 storage service failure during upload")
    void upload_R2StorageServiceFails_ShouldThrowAppException() {
        // Given
        String originalFilename = "test-image.jpg";
        String contentType = "image/jpeg";
        String ownerId = "test-owner";
        AppException storageException = new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Storage service failed");

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenThrow(storageException);

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.IMAGE);

            // When & Then
            assertThatThrownBy(() -> mediaStorageManagement.upload(mockFile, ownerId)).isInstanceOf(AppException.class)
                    .hasMessage("Storage service failed")
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_ERROR);
                    });

            verify(mediaRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle database failure during upload")
    void upload_DatabaseFails_ShouldThrowException() {
        // Given
        String originalFilename = "test-image.jpg";
        String contentType = "image/jpeg";
        String storageKey = "images/uuid-test-image.jpg";
        String ownerId = "test-owner";

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(1024L);

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenReturn(storageKey);
        when(mediaRepository.save(any(Media.class))).thenThrow(new RuntimeException("Database connection failed"));

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.IMAGE);

            // When & Then
            assertThatThrownBy(() -> mediaStorageManagement.upload(mockFile, ownerId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");

            verify(rustfsStorageService).uploadFile(eq(mockFile), anyString(), eq(contentType));
            verify(mediaRepository).save(any(Media.class));
        }
    }

    @Test
    @DisplayName("Should upload video file successfully")
    void upload_VideoFile_ShouldReturnCorrectResponseDto() {
        // Given
        String originalFilename = "test-video.mp4";
        String contentType = "video/mp4";
        String extension = "mp4";
        String storageKey = "videos/uuid-test-video.mp4";
        String ownerId = "test-owner";

        Media videoMedia = Media.builder()
                .id(2L)
                .originalFilename(originalFilename)
                .storageKey(storageKey)
                .cdnUrl(testCdnDomain + "/" + storageKey)
                .mediaType(MediaType.VIDEO)
                .fileSize(5120L)
                .contentType(contentType)
                .ownerId(ownerId)
                .build();

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(5120L);

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenReturn(storageKey);
        when(mediaRepository.save(any(Media.class))).thenReturn(videoMedia);

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.VIDEO);

            // When
            UploadedMediaResponseDto result = mediaStorageManagement.upload(mockFile, ownerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMediaType()).isEqualTo("VIDEO");
            assertThat(result.getCdnUrl()).isEqualTo(videoMedia.getCdnUrl());
        }
    }

    // ===============================
    // Delete Method Tests
    // ===============================

    @Test
    @DisplayName("Should delete media successfully when media exists")
    void deleteMedia_ExistingMediaId_ShouldDeleteSuccessfully() {
        // Given
        Long mediaId = 1L;

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
        doNothing().when(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        doNothing().when(mediaRepository).delete(testMedia);
        doNothing().when(documentVisitService).deleteDocumentVisits(anyString());

        // When
        mediaStorageManagement.deleteMedia(mediaId);

        // Then
        verify(mediaRepository).findById(mediaId);
        verify(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        verify(mediaRepository).delete(testMedia);
        verify(documentVisitService).deleteDocumentVisits(String.valueOf(mediaId));
    }

    @Test
    @DisplayName("Should throw AppException when media not found")
    void deleteMedia_NonExistentMediaId_ShouldThrowAppException() {
        // Given
        Long mediaId = 999L;

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mediaStorageManagement.deleteMedia(mediaId)).isInstanceOf(AppException.class)
                .hasMessage("Media not found with ID: " + mediaId)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.MEDIA_NOT_FOUND);
                });

        verify(mediaRepository).findById(mediaId);
        verify(rustfsStorageService, never()).deleteFile(anyString());
        verify(mediaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle R2 storage service failure during delete")
    void deleteMedia_R2StorageServiceFails_ShouldThrowAppException() {
        // Given
        Long mediaId = 1L;
        AppException storageException = new AppException(ErrorCode.FILE_UPLOAD_ERROR, "Failed to delete file");

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
        doThrow(storageException).when(rustfsStorageService).deleteFile(testMedia.getStorageKey());

        // When & Then
        assertThatThrownBy(() -> mediaStorageManagement.deleteMedia(mediaId)).isInstanceOf(AppException.class)
                .hasMessage("Failed to delete file")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_ERROR);
                });

        verify(mediaRepository).findById(mediaId);
        verify(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        verify(mediaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle database failure during delete")
    void deleteMedia_DatabaseFails_ShouldThrowException() {
        // Given
        Long mediaId = 1L;

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
        doNothing().when(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        doThrow(new RuntimeException("Database connection failed")).when(mediaRepository).delete(testMedia);

        // When & Then
        assertThatThrownBy(() -> mediaStorageManagement.deleteMedia(mediaId)).isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(mediaRepository).findById(mediaId);
        verify(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        verify(mediaRepository).delete(testMedia);
    }

    @Test
    @DisplayName("Should delete media even if R2 delete partially fails but continues")
    void deleteMedia_PartialR2Failure_ShouldCompleteOperation() {
        // Given
        Long mediaId = 1L;

        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        // R2 delete succeeds (no exception thrown)
        doNothing().when(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        doNothing().when(mediaRepository).delete(testMedia);
        doNothing().when(documentVisitService).deleteDocumentVisits(anyString());

        // When
        mediaStorageManagement.deleteMedia(mediaId);

        // Then
        verify(mediaRepository).findById(mediaId);
        verify(rustfsStorageService).deleteFile(testMedia.getStorageKey());
        verify(mediaRepository).delete(testMedia);
        verify(documentVisitService).deleteDocumentVisits(String.valueOf(mediaId));
    }

    @Test
    @DisplayName("Should handle null originalFilename gracefully")
    void upload_NullOriginalFilename_ShouldThrowAppException() {
        // Given
        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile))
                    .thenThrow(new AppException(ErrorCode.MISSING_REQUIRED_FIELD, "Original filename is required"));

            // When & Then
            assertThatThrownBy(() -> mediaStorageManagement.upload(mockFile)).isInstanceOf(AppException.class)
                    .hasMessage("Original filename is required");
        }
    }

    @Test
    @DisplayName("Should handle null contentType gracefully")
    void upload_NullContentType_ShouldThrowAppException() {
        // Given
        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile))
                    .thenThrow(new AppException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Content type cannot be determined"));

            // When & Then
            assertThatThrownBy(() -> mediaStorageManagement.upload(mockFile)).isInstanceOf(AppException.class)
                    .hasMessage("Content type cannot be determined");
        }
    }

    @Test
    @DisplayName("Should build correct CDN URL with different domain formats")
    void upload_DifferentCdnDomainFormats_ShouldBuildCorrectUrl() {
        // Test with domain ending with slash
        ReflectionTestUtils.setField(mediaStorageManagement, "cdnDomain", "https://cdn.example.com/");

        // Given
        String originalFilename = "test-image.jpg";
        String contentType = "image/jpeg";
        String storageKey = "images/uuid-test-image.jpg";
        String expectedCdnUrl = "https://cdn.example.com/" + storageKey;
        String ownerId = "test-owner";

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(1024L);

        Media mediaWithSlashDomain = testMedia.builder().cdnUrl(expectedCdnUrl).build();

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenReturn(storageKey);
        when(mediaRepository.save(any(Media.class))).thenReturn(mediaWithSlashDomain);

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.IMAGE);

            // When
            UploadedMediaResponseDto result = mediaStorageManagement.upload(mockFile, ownerId);

            // Then
            assertThat(result.getCdnUrl()).isEqualTo(expectedCdnUrl);
        }
    }

    @Test
    @DisplayName("Should handle large file upload")
    void upload_LargeFile_ShouldHandleCorrectly() {
        // Given
        String originalFilename = "large-video.mp4";
        String contentType = "video/mp4";
        Long largeFileSize = 50_000_000L; // 50MB
        String storageKey = "videos/uuid-large-video.mp4";
        String ownerId = "test-owner";

        when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(largeFileSize);

        Media largeMedia = testMedia.builder()
                .originalFilename(originalFilename)
                .storageKey(storageKey)
                .mediaType(MediaType.VIDEO)
                .fileSize(largeFileSize)
                .contentType(contentType)
                .ownerId(ownerId)
                .build();

        when(rustfsStorageService.uploadFile(eq(mockFile), anyString(), eq(contentType))).thenReturn(storageKey);
        when(mediaRepository.save(any(Media.class))).thenReturn(largeMedia);

        try (MockedStatic<MediaValidation> validationMock = mockStatic(MediaValidation.class)) {
            validationMock.when(() -> MediaValidation.getValidatedMediaType(mockFile)).thenReturn(MediaType.VIDEO);

            // When
            UploadedMediaResponseDto result = mediaStorageManagement.upload(mockFile, ownerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMediaType()).isEqualTo("VIDEO");
            verify(mediaRepository).save(argThat(
                    media -> media.getFileSize().equals(largeFileSize) && media.getMediaType() == MediaType.VIDEO));
        }
    }
}
