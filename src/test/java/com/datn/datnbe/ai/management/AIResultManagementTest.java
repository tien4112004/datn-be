package com.datn.datnbe.ai.management;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datn.datnbe.ai.dto.response.AIResultResponseDto;
import com.datn.datnbe.ai.entity.AIResult;
import com.datn.datnbe.ai.mapper.AIResultMapper;
import com.datn.datnbe.ai.repository.AIResultRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AIResultManagementTest {

    @Mock
    private AIResultRepository aiResultRepo;

    @Mock
    private AIResultMapper aiResultMapper;

    private AIResultManagement aiResultManagement;

    private AIResult mockAIResult;
    private String testPresentationId;
    private String testAIResultContent;
    private Date testCreatedAt;

    @BeforeEach
    void setUp() {
        aiResultManagement = new AIResultManagement(aiResultRepo, aiResultMapper);

        // Setup test data
        testPresentationId = "presentation-123";
        testAIResultContent = "Test AI generated content for slides";
        testCreatedAt = Date.from(Instant.parse("2025-09-09T10:30:00Z"));

        mockAIResult = AIResult.builder()
                .id(1)
                .result(testAIResultContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt)
                .build();
    }

    @Test
    @DisplayName("Should save AI result successfully")
    void testSaveAIResult_Success() {
        // Arrange
        AIResultResponseDto expectedResponse = AIResultResponseDto.builder()
                .id(1)
                .result(testAIResultContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt.toString())
                .build();

        when(aiResultRepo.save(any(AIResult.class))).thenReturn(mockAIResult);
        when(aiResultMapper.toResponseDto(mockAIResult)).thenReturn(expectedResponse);

        // Act
        AIResultResponseDto result = aiResultManagement.saveAIResult(testAIResultContent, testPresentationId, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(testAIResultContent, result.getResult());
        assertEquals(testPresentationId, result.getPresentationId());
        assertEquals(testCreatedAt.toString(), result.getCreatedAt());

        // Verify repository interaction
        verify(aiResultRepo, times(1)).save(argThat(entity -> entity.getResult().equals(testAIResultContent)
                && entity.getPresentationId().equals(testPresentationId) && entity.getId() == null // Should be null before saving
        ));
        verify(aiResultMapper, times(1)).toResponseDto(mockAIResult);
    }

    @Test
    @DisplayName("Should save AI result with null content")
    void testSaveAIResult_WithNullContent() {
        // Arrange
        String nullContent = null;
        AIResult savedEntity = AIResult.builder()
                .id(1)
                .result(nullContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt)
                .build();

        AIResultResponseDto expectedResponse = AIResultResponseDto.builder()
                .id(1)
                .result(nullContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt.toString())
                .build();

        when(aiResultRepo.save(any(AIResult.class))).thenReturn(savedEntity);
        when(aiResultMapper.toResponseDto(savedEntity)).thenReturn(expectedResponse);

        // Act
        AIResultResponseDto result = aiResultManagement.saveAIResult(nullContent, testPresentationId, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getResult());
        assertEquals(testPresentationId, result.getPresentationId());
        assertEquals(testCreatedAt.toString(), result.getCreatedAt());

        verify(aiResultRepo, times(1)).save(any(AIResult.class));
        verify(aiResultMapper, times(1)).toResponseDto(savedEntity);
    }

    @Test
    @DisplayName("Should save AI result with empty content")
    void testSaveAIResult_WithEmptyContent() {
        // Arrange
        String emptyContent = "";
        AIResult savedEntity = AIResult.builder()
                .id(1)
                .result(emptyContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt)
                .build();

        AIResultResponseDto expectedResponse = AIResultResponseDto.builder()
                .id(1)
                .result(emptyContent)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt.toString())
                .build();

        when(aiResultRepo.save(any(AIResult.class))).thenReturn(savedEntity);
        when(aiResultMapper.toResponseDto(savedEntity)).thenReturn(expectedResponse);

        // Act
        AIResultResponseDto result = aiResultManagement.saveAIResult(emptyContent, testPresentationId, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("", result.getResult());
        assertEquals(testPresentationId, result.getPresentationId());

        verify(aiResultRepo, times(1)).save(any(AIResult.class));
        verify(aiResultMapper, times(1)).toResponseDto(savedEntity);
    }

    @Test
    @DisplayName("Should get AI result by presentation ID successfully")
    void testGetAIResultByPresentationId_Success() {
        // Arrange
        when(aiResultRepo.findByPresentationId(testPresentationId)).thenReturn(Optional.of(mockAIResult));

        // Act
        AIResultResponseDto result = aiResultManagement.getAIResultByPresentationId(testPresentationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(testAIResultContent, result.getResult());
        assertEquals(testPresentationId, result.getPresentationId());
        assertEquals(testCreatedAt.toString(), result.getCreatedAt());

        verify(aiResultRepo, times(1)).findByPresentationId(testPresentationId);
    }

    @Test
    @DisplayName("Should throw exception when AI result not found by presentation ID")
    void testGetAIResultByPresentationId_NotFound() {
        // Arrange
        String nonExistentPresentationId = "non-existent-123";
        when(aiResultRepo.findByPresentationId(nonExistentPresentationId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> aiResultManagement.getAIResultByPresentationId(nonExistentPresentationId));

        assertEquals(ErrorCode.AI_RESULT_NOT_FOUND, exception.getErrorCode());
        assertEquals("AI Result with presentation ID " + nonExistentPresentationId + " not found",
                exception.getMessage());

        verify(aiResultRepo, times(1)).findByPresentationId(nonExistentPresentationId);
    }

    @Test
    @DisplayName("Should get AI result with null content")
    void testGetAIResultByPresentationId_WithNullContent() {
        // Arrange
        AIResult resultWithNullContent = AIResult.builder()
                .id(1)
                .result(null)
                .presentationId(testPresentationId)
                .createdAt(testCreatedAt)
                .build();

        when(aiResultRepo.findByPresentationId(testPresentationId)).thenReturn(Optional.of(resultWithNullContent));

        // Act
        AIResultResponseDto result = aiResultManagement.getAIResultByPresentationId(testPresentationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getResult());
        assertEquals(testPresentationId, result.getPresentationId());
        assertEquals(testCreatedAt.toString(), result.getCreatedAt());

        verify(aiResultRepo, times(1)).findByPresentationId(testPresentationId);
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void testSaveAIResult_RepositoryFailure() {
        // Arrange
        when(aiResultRepo.save(any(AIResult.class))).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            aiResultManagement.saveAIResult(testAIResultContent, testPresentationId, null));

        assertEquals("Database connection failed", exception.getMessage());
        verify(aiResultRepo, times(1)).save(any(AIResult.class));
    }

    @Test
    @DisplayName("Should handle repository find failure")
    void testGetAIResultByPresentationId_RepositoryFailure() {
        // Arrange
        when(aiResultRepo.findByPresentationId(testPresentationId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            aiResultManagement.getAIResultByPresentationId(testPresentationId));

        assertEquals("Database connection failed", exception.getMessage());
        verify(aiResultRepo, times(1)).findByPresentationId(testPresentationId);
    }
}
