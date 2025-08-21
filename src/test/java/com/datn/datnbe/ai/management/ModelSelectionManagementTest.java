package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModelSelectionManagementTest {

    @Mock
    private ModelConfigurationRepo modelConfigurationRepo;
    private ModelDataMapper modelDataMapper;

    private ModelSelectionManagement modelSelectionService;

    private ModelConfigurationEntity modelEntity1;
    private ModelConfigurationEntity modelEntity2;
    private ModelConfigurationEntity modelEntity3;
    private ModelResponseDto responseDto;
    private ModelProperties.ModelInfo modelInfo;
    private UpdateModelStatusRequest updateModelStatusRequest;

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
        modelEntity1.setProvider("openai");

        modelEntity2 = new ModelConfigurationEntity();
        modelEntity2.setModelId(2);
        modelEntity2.setModelName("claude-3");
        modelEntity2.setDisplayName("Claude 3");
        modelEntity2.setEnabled(false);
        modelEntity2.setDefault(true);
        modelEntity2.setProvider("anthropic");

        modelEntity3 = new ModelConfigurationEntity();
        modelEntity3.setModelId(3);
        modelEntity3.setModelName("gemini-pro");
        modelEntity3.setDisplayName("Gemini Pro");
        modelEntity3.setEnabled(true);
        modelEntity3.setDefault(false);
        modelEntity3.setProvider("google");

        responseDto = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .isEnabled(true)
                .isDefault(false)
                .build();

        // Setup test ModelInfo
        modelInfo = new ModelProperties.ModelInfo();
        modelInfo.setModelName("new-model");
        modelInfo.setDisplayName("New Model");
        modelInfo.setProvider("test-provider");
        modelInfo.setDefaultModel(true);

        // Setup UpdateModelStatusRequest - using reflection or helper method since
        // fields are final
        updateModelStatusRequest = createUpdateModelStatusRequest(true, false);
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
        when(modelConfigurationRepo.getModelByName(modelName)).thenReturn(modelEntity1);
        when(modelConfigurationRepo.isModelEnabled(modelEntity1.getModelId())).thenReturn(true);

        // When
        boolean result = modelSelectionService.isModelEnabled(modelName);

        // Then
        assertTrue(result);
        verify(modelConfigurationRepo).getModelByName(modelName);
        verify(modelConfigurationRepo).isModelEnabled(modelEntity1.getModelId());
    }

    @Test
    @DisplayName("Should return false when model is disabled")
    void isModelEnabled_ModelDisabled() {
        // Given
        String modelName = "claude-3";
        when(modelConfigurationRepo.getModelByName(modelName)).thenReturn(modelEntity2);
        when(modelConfigurationRepo.isModelEnabled(modelEntity2.getModelId())).thenReturn(false);

        // When
        boolean result = modelSelectionService.isModelEnabled(modelName);

        // Then
        assertFalse(result);
        verify(modelConfigurationRepo).getModelByName(modelName);
        verify(modelConfigurationRepo).isModelEnabled(modelEntity2.getModelId());
    }

    @Test
    @DisplayName("Should throw AppException when model not found for isModelEnabled")
    void isModelEnabled_ModelNotFound() {
        // Given
        String modelName = "non-existent-model";
        when(modelConfigurationRepo.getModelByName(modelName)).thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.isModelEnabled(modelName));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
        verify(modelConfigurationRepo, never()).isModelEnabled(anyInt());
    }

    @Test
    @DisplayName("Should save model info successfully")
    void saveModelInfo_Success() {

        // Given
        ModelConfigurationEntity mappedEntity = new ModelConfigurationEntity();
        mappedEntity.setModelName(modelInfo.getModelName());
        mappedEntity.setDisplayName(modelInfo.getDisplayName());
        mappedEntity.setProvider(modelInfo.getProvider());

        // When
        assertDoesNotThrow(() -> modelSelectionService.saveModelInfo(modelInfo));

        // Then
        verify(modelConfigurationRepo)
                .save(argThat(entity -> entity.isEnabled() && entity.isDefault() == modelInfo.isDefaultModel()
                        && entity.getModelName().equals(modelInfo.getModelName())));
    }

    @Test
    @DisplayName("Should save model info with correct default setting")
    void saveModelInfo_WithNonDefaultModel() {
        // Given
        modelInfo.setDefaultModel(false);
        ModelConfigurationEntity mappedEntity = new ModelConfigurationEntity();
        mappedEntity.setModelName(modelInfo.getModelName());
        mappedEntity.setDisplayName(modelInfo.getDisplayName());
        mappedEntity.setProvider(modelInfo.getProvider());

        // When
        assertDoesNotThrow(() -> modelSelectionService.saveModelInfo(modelInfo));

        // Then
        verify(modelConfigurationRepo).save(argThat(entity -> entity.isEnabled() && !entity.isDefault()
                && entity.getModelName().equals(modelInfo.getModelName())));
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
    // Tests for setModelStatus
    // ===============================

    @Test
    @DisplayName("Should update model enabled status when only isEnabled is provided")
    void setModelStatus_OnlyEnabledProvided_Success() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(true, null);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);

        // When
        ModelResponseDto result = modelSelectionService.setModelStatus(modelId, request);

        // Then
        assertNotNull(result);
        verify(modelConfigurationRepo).setEnabled(modelId, true);
        verify(modelConfigurationRepo, never()).setDefault(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo).getModelById(modelId);
    }

    @Test
    @DisplayName("Should update model default status when only isDefault is provided")
    void setModelStatus_OnlyDefaultProvided_Success() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(null, true);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);

        // When
        ModelResponseDto result = modelSelectionService.setModelStatus(modelId, request);

        // Then
        assertNotNull(result);
        verify(modelConfigurationRepo).setDefault(modelId, true);
        verify(modelConfigurationRepo, never()).setEnabled(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo).getModelById(modelId);
    }

    @Test
    @DisplayName("Should update both enabled and default status when both are provided")
    void setModelStatus_BothProvided_Success() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(true, true);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);

        // When
        ModelResponseDto result = modelSelectionService.setModelStatus(modelId, request);

        // Then
        assertNotNull(result);
        verify(modelConfigurationRepo).setEnabled(modelId, true);
        verify(modelConfigurationRepo).setDefault(modelId, true);
        verify(modelConfigurationRepo).getModelById(modelId);
    }

    @Test
    @DisplayName("Should throw exception when trying to set disabled model as default")
    void setModelStatus_DisabledAndDefault_ThrowsException() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(false, true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelStatus(modelId, request));

        assertEquals(ErrorCode.INVALID_MODEL_STATUS, exception.getErrorCode());
        assertEquals("A model cannot be default if it is disabled", exception.getMessage());
        verify(modelConfigurationRepo, never()).setEnabled(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo, never()).setDefault(anyInt(), any(Boolean.class));
    }

    @Test
    @DisplayName("Should throw exception when both isEnabled and isDefault are null")
    void setModelStatus_BothNull_ThrowsException() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(null, null);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelStatus(modelId, request));

        assertEquals(ErrorCode.INVALID_MODEL_STATUS, exception.getErrorCode());
        assertEquals("At least one of isEnabled or isDefault must be provided", exception.getMessage());
        verify(modelConfigurationRepo, never()).setEnabled(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo, never()).setDefault(anyInt(), any(Boolean.class));
    }

    @Test
    @DisplayName("Should handle model not found in setModelStatus")
    void setModelStatus_ModelNotFound_ThrowsException() {
        // Given
        Integer modelId = 999;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(true, false);
        when(modelConfigurationRepo.getModelById(modelId)).thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelStatus(modelId, request));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
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
        when(modelConfigurationRepo.getModels()).thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> modelSelectionService.getModelConfigurations());

        assertEquals("Database connection error", exception.getMessage());
        verify(modelConfigurationRepo).getModels();
    }

    @Test
    @DisplayName("Should handle repository exception during model save")
    void saveModelInfo_RepositoryException_ThrowsException() {
        // Given
        when(modelConfigurationRepo.save(any(ModelConfigurationEntity.class)))
                .thenThrow(new RuntimeException("Database save error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> modelSelectionService.saveModelInfo(modelInfo));

        assertEquals("Database save error", exception.getMessage());
        verify(modelConfigurationRepo).save(any(ModelConfigurationEntity.class));
    }

    @Test
    @DisplayName("Should disable model successfully when enabled is false and default is null")
    void setModelStatus_DisableModel_Success() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(false, null);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);

        // When
        ModelResponseDto result = modelSelectionService.setModelStatus(modelId, request);

        // Then
        assertNotNull(result);
        verify(modelConfigurationRepo).setEnabled(modelId, false);
        verify(modelConfigurationRepo, never()).setDefault(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo).getModelById(modelId);
    }

    @Test
    @DisplayName("Should set model as non-default successfully when default is false and enabled is null")
    void setModelStatus_SetNonDefault_Success() {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = createUpdateModelStatusRequest(null, false);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);

        // When
        ModelResponseDto result = modelSelectionService.setModelStatus(modelId, request);

        // Then
        assertNotNull(result);
        verify(modelConfigurationRepo).setDefault(modelId, false);
        verify(modelConfigurationRepo, never()).setEnabled(anyInt(), any(Boolean.class));
        verify(modelConfigurationRepo).getModelById(modelId);
    }
}