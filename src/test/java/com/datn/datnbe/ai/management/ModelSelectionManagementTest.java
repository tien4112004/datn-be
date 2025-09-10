package com.datn.datnbe.ai.management;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelSelectionManagementTest {

    @Mock
    private ModelConfigurationRepo modelConfigurationRepo;
    private ModelDataMapper modelDataMapper;

    private ModelSelectionManagement modelSelectionService;

    private ModelConfigurationEntity modelEntity1;
    private ModelConfigurationEntity modelEntity2;
    private ModelConfigurationEntity modelEntity3;
    private ModelConfigurationEntity textModel1;
    private ModelConfigurationEntity textModel2;
    private ModelConfigurationEntity imageModel1;
    private ModelConfigurationEntity imageModel2;
    private ModelProperties.ModelInfo modelInfo;

    @BeforeEach
    void setUp() {
        modelDataMapper = Mappers.getMapper(ModelDataMapper.class);
        modelSelectionService = new ModelSelectionManagement(modelConfigurationRepo, modelDataMapper);

        // Setup test entities
        modelEntity1 = new ModelConfigurationEntity();
        modelEntity1.setModelId(1);
        modelEntity1.setModelName("gpt-4");
        modelEntity1.setDisplayName("GPT-4");
        modelEntity1.setEnabled(true);
        modelEntity1.setDefault(false);
        modelEntity1.setModelType(ModelType.TEXT);
        modelEntity1.setProvider("openai");

        modelEntity2 = new ModelConfigurationEntity();
        modelEntity2.setModelId(2);
        modelEntity2.setModelName("claude-3");
        modelEntity2.setDisplayName("Claude 3");
        modelEntity2.setEnabled(false);
        modelEntity2.setDefault(true);
        modelEntity2.setModelType(ModelType.TEXT);
        modelEntity2.setProvider("anthropic");

        modelEntity3 = new ModelConfigurationEntity();
        modelEntity3.setModelId(3);
        modelEntity3.setModelName("gemini-pro");
        modelEntity3.setDisplayName("Gemini Pro");
        modelEntity3.setEnabled(true);
        modelEntity3.setDefault(false);
        modelEntity3.setModelType(ModelType.TEXT);
        modelEntity3.setProvider("google");

        // Setup test ModelInfo
        modelInfo = new ModelProperties.ModelInfo();
        modelInfo.setModelName("new-model");
        modelInfo.setDisplayName("New Model");
        modelInfo.setProvider("test-provider");
        modelInfo.setDefaultModel(true);

        // Setup text models for text/image specific tests
        textModel1 = new ModelConfigurationEntity();
        textModel1.setModelId(10);
        textModel1.setModelName("gpt-4-text");
        textModel1.setDisplayName("GPT-4 Text");
        textModel1.setProvider("openai");
        textModel1.setEnabled(true);
        textModel1.setDefault(true);
        textModel1.setModelType(ModelType.TEXT);

        textModel2 = new ModelConfigurationEntity();
        textModel2.setModelId(11);
        textModel2.setModelName("claude-3-text");
        textModel2.setDisplayName("Claude 3 Text");
        textModel2.setProvider("anthropic");
        textModel2.setEnabled(true);
        textModel2.setDefault(false);
        textModel2.setModelType(ModelType.TEXT);

        // Setup image models for text/image specific tests
        imageModel1 = new ModelConfigurationEntity();
        imageModel1.setModelId(20);
        imageModel1.setModelName("dall-e-3");
        imageModel1.setDisplayName("DALL-E 3");
        imageModel1.setProvider("openai");
        imageModel1.setEnabled(true);
        imageModel1.setDefault(true);
        imageModel1.setModelType(ModelType.IMAGE);

        imageModel2 = new ModelConfigurationEntity();
        imageModel2.setModelId(21);
        imageModel2.setModelName("midjourney");
        imageModel2.setDisplayName("Midjourney");
        imageModel2.setProvider("midjourney");
        imageModel2.setEnabled(false);
        imageModel2.setDefault(false);
        imageModel2.setModelType(ModelType.IMAGE);
    }

    // Helper method to create UpdateModelStatusRequest instances
    private UpdateModelStatusRequest createUpdateModelStatusRequest(Boolean isEnable, Boolean isDefault) {
        return new UpdateModelStatusRequest(isEnable, isDefault);
    }

    @Test
    @DisplayName("Should return true when model is enabled")
    void isModelEnabled_ModelEnabled() {
        // Given
        String modelName = "gpt-4";
        when(modelConfigurationRepo.getModelByTextName(modelName)).thenReturn(modelEntity1);
        when(modelConfigurationRepo.isModelEnabled(modelEntity1.getModelId())).thenReturn(true);

        // When
        boolean result = modelSelectionService.isModelEnabled(modelName);

        // Then
        assertTrue(result);
        verify(modelConfigurationRepo).getModelByTextName(modelName);
        verify(modelConfigurationRepo).isModelEnabled(modelEntity1.getModelId());
    }

    @Test
    @DisplayName("Should return false when model is disabled")
    void isModelEnabled_ModelDisabled() {
        // Given
        String modelName = "claude-3";
        when(modelConfigurationRepo.getModelByTextName(modelName)).thenReturn(modelEntity2);
        when(modelConfigurationRepo.isModelEnabled(modelEntity2.getModelId())).thenReturn(false);

        // When
        boolean result = modelSelectionService.isModelEnabled(modelName);

        // Then
        assertFalse(result);
        verify(modelConfigurationRepo).getModelByTextName(modelName);
        verify(modelConfigurationRepo).isModelEnabled(modelEntity2.getModelId());
    }

    @Test
    @DisplayName("Should throw AppException when model not found for isModelEnabled")
    void isModelEnabled_ModelNotFound() {
        // Given
        String modelName = "non-existent-model";
        when(modelConfigurationRepo.getModelByTextName(modelName))
                .thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.isModelEnabled(modelName));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
        verify(modelConfigurationRepo, never()).isModelEnabled(anyInt());
    }

    // ===============================
    // Tests for getModelConfigurations
    // ===============================

    @Test
    @DisplayName("Should return sorted list of model configurations")
    void getModelConfigurations_Success() {
        // Given
        List<ModelConfigurationEntity> models = Arrays.asList(modelEntity3, modelEntity2, modelEntity1); // Not sorted
        // by provider
        when(modelConfigurationRepo.getModels()).thenReturn(models);

        // When
        List<ModelResponseDto> result = modelSelectionService.getModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(modelConfigurationRepo).getModels();

        // Verify the models are sorted by provider (anthropic, google, openai)
        assertEquals("claude-3", result.get(0).getModelName());
        assertEquals("gemini-pro", result.get(1).getModelName());
        assertEquals("gpt-4", result.get(2).getModelName());
    }

    @Test
    @DisplayName("Should return empty list when no models exist")
    void getModelConfigurations_EmptyList() {
        // Given
        when(modelConfigurationRepo.getModels()).thenReturn(Arrays.asList());

        // When
        List<ModelResponseDto> result = modelSelectionService.getModelConfigurations();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(modelConfigurationRepo).getModels();
    }

    // ===============================
    // Tests for existByName
    // ===============================

    @Test
    @DisplayName("Should return true when model exists by name")
    void existByName_ModelExists_ReturnsTrue() {
        // Given
        String modelName = "gpt-4";
        when(modelConfigurationRepo.existsByModelName(modelName)).thenReturn(true);

        // When
        boolean result = modelSelectionService.existByName(modelName);

        // Then
        assertTrue(result);
        verify(modelConfigurationRepo).existsByModelName(modelName);
    }

    @Test
    @DisplayName("Should return false when model does not exist by name")
    void existByName_ModelDoesNotExist_ReturnsFalse() {
        // Given
        String modelName = "non-existent-model";
        when(modelConfigurationRepo.existsByModelName(modelName)).thenReturn(false);

        // When
        boolean result = modelSelectionService.existByName(modelName);

        // Then
        assertFalse(result);
        verify(modelConfigurationRepo).existsByModelName(modelName);
    }

    // ===============================
    // Tests for removeModelByName
    // ===============================

    @Test
    @DisplayName("Should remove model when it exists")
    void removeModelByName_ModelExists_Success() {
        // Given
        String modelName = "gpt-4";
        when(modelConfigurationRepo.existsByModelName(modelName)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> modelSelectionService.removeModelByName(modelName));

        // Then
        verify(modelConfigurationRepo).existsByModelName(modelName);
        verify(modelConfigurationRepo).deleteByModelName(modelName);
    }

    @Test
    @DisplayName("Should not attempt to remove model when it does not exist")
    void removeModelByName_ModelDoesNotExist_NoAction() {
        // Given
        String modelName = "non-existent-model";
        when(modelConfigurationRepo.existsByModelName(modelName)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> modelSelectionService.removeModelByName(modelName));

        // Then
        verify(modelConfigurationRepo).existsByModelName(modelName);
        verify(modelConfigurationRepo, never()).deleteByModelName(anyString());
    }

    // ===============================
    // Additional edge case tests
    // ===============================

    @Test
    @DisplayName("Should handle repository exception during model configuration retrieval")
    void getModelConfigurations_RepositoryException_ThrowsException() {
        // Given
        when(modelConfigurationRepo.getModels())
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> modelSelectionService.getModelConfigurations());

        assertEquals("Database connection error", exception.getMessage());
        verify(modelConfigurationRepo).getModels();
    }

    // ===============================
    // Tests for getTextModelModelConfigurations
    // ===============================

    @Test
    @DisplayName("Should return sorted list of text model configurations")
    void getTextModelConfigurations_WithValidData_ShouldReturnSortedList() {
        // Given
        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1, textModel2);
        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);

        // When
        List<ModelResponseDto> result = modelSelectionService.getTextModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify sorting by provider (anthropic comes before openai)
        assertEquals("anthropic", result.get(0).getProvider());
        assertEquals("claude-3-text", result.get(0).getModelName());
        assertEquals("Claude 3 Text", result.get(0).getDisplayName());
        assertTrue(result.get(0).isEnabled());
        assertFalse(result.get(0).isDefault());

        assertEquals("openai", result.get(1).getProvider());
        assertEquals("gpt-4-text", result.get(1).getModelName());
        assertEquals("GPT-4 Text", result.get(1).getDisplayName());
        assertTrue(result.get(1).isEnabled());
        assertTrue(result.get(1).isDefault());
    }

    @Test
    @DisplayName("Should return empty list when no text models exist")
    void getTextModelModelConfigurations_WithNoTextModels_ShouldReturnEmptyList() {
        // Given
        when(modelConfigurationRepo.getTextModels()).thenReturn(Arrays.asList());

        // When
        List<ModelResponseDto> result = modelSelectionService.getTextModelConfigurations();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return single text model configuration")
    void getTextModelModelConfigurations_WithSingleTextModel_ShouldReturnSingleItem() {
        // Given
        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1);
        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);

        // When
        List<ModelResponseDto> result = modelSelectionService.getTextModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("gpt-4-text", result.get(0).getModelName());
        assertEquals("GPT-4 Text", result.get(0).getDisplayName());
        assertEquals("openai", result.get(0).getProvider());
        assertTrue(result.get(0).isEnabled());
        assertTrue(result.get(0).isDefault());
    }

    @Test
    @DisplayName("Should handle repository exception for text models")
    void getTextModelConfigurations_WithRepositoryException_ShouldThrowException() {
        // Given
        when(modelConfigurationRepo.getTextModels()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> modelSelectionService.getTextModelConfigurations());
        assertEquals("Database connection failed", exception.getMessage());
    }

    // ===============================
    // Tests for getImageModelConfigurations
    // ===============================

    @Test
    @DisplayName("Should return sorted list of image model configurations")
    void getImageModelConfigurations_WithValidData_ShouldReturnSortedList() {
        // Given
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel1, imageModel2);
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionService.getImageModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify sorting by provider (midjourney comes before openai)
        assertEquals("midjourney", result.get(0).getProvider());
        assertEquals("midjourney", result.get(0).getModelName());
        assertEquals("Midjourney", result.get(0).getDisplayName());
        assertFalse(result.get(0).isEnabled());
        assertFalse(result.get(0).isDefault());

        assertEquals("openai", result.get(1).getProvider());
        assertEquals("dall-e-3", result.get(1).getModelName());
        assertEquals("DALL-E 3", result.get(1).getDisplayName());
        assertTrue(result.get(1).isEnabled());
        assertTrue(result.get(1).isDefault());
    }

    @Test
    @DisplayName("Should return empty list when no image models exist")
    void getImageModelConfigurations_WithNoImageModels_ShouldReturnEmptyList() {
        // Given
        when(modelConfigurationRepo.getImageModels()).thenReturn(Arrays.asList());

        // When
        List<ModelResponseDto> result = modelSelectionService.getImageModelConfigurations();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return single image model configuration")
    void getImageModelConfigurations_WithSingleImageModel_ShouldReturnSingleItem() {
        // Given
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel1);
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionService.getImageModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("dall-e-3", result.get(0).getModelName());
        assertEquals("DALL-E 3", result.get(0).getDisplayName());
        assertEquals("openai", result.get(0).getProvider());
        assertTrue(result.get(0).isEnabled());
        assertTrue(result.get(0).isDefault());
    }

    @Test
    @DisplayName("Should handle disabled image models")
    void getImageModelConfigurations_WithDisabledModels_ShouldIncludeDisabledModels() {
        // Given
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel2); // imageModel2 is disabled
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionService.getImageModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("midjourney", result.get(0).getModelName());
        assertFalse(result.get(0).isEnabled());
        assertFalse(result.get(0).isDefault());
    }

    @Test
    @DisplayName("Should handle repository exception for image models")
    void getImageModelConfigurations_WithRepositoryException_ShouldThrowException() {
        // Given
        when(modelConfigurationRepo.getImageModels()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> modelSelectionService.getImageModelConfigurations());
        assertEquals("Database connection failed", exception.getMessage());
    }

    // ===============================
    // Integration tests between text and image
    // ===============================

    @Test
    @DisplayName("Should return different results for text vs image models")
    void getModelConfigurations_TextVsImage_ShouldReturnDifferentResults() {
        // Given
        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1, textModel2);
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel1, imageModel2);

        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> textResult = modelSelectionService.getTextModelConfigurations();
        List<ModelResponseDto> imageResult = modelSelectionService.getImageModelConfigurations();

        // Then
        assertEquals(2, textResult.size());
        assertEquals(2, imageResult.size());

        // Verify they contain different models
        List<String> textModelNames = textResult.stream().map(ModelResponseDto::getModelName).toList();
        List<String> imageModelNames = imageResult.stream().map(ModelResponseDto::getModelName).toList();

        assertTrue(textModelNames.contains("gpt-4-text"));
        assertTrue(textModelNames.contains("claude-3-text"));
        assertTrue(imageModelNames.contains("dall-e-3"));
        assertTrue(imageModelNames.contains("midjourney"));

        // Ensure no overlap
        textModelNames.forEach(name -> assertFalse(imageModelNames.contains(name)));
    }
}
