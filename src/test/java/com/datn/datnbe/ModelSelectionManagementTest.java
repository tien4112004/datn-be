package com.datn.datnbe;

import com.datn.datnbe.ai.management.ModelSelectionManagement;
import com.datn.datnbe.ai.config.chatmodelconfiguration.ModelProperties.ModelInfo;
import com.datn.datnbe.ai.dto.response.ModelMinimalResponseDto;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.gateway.exceptions.AppException;
import com.datn.datnbe.gateway.exceptions.ErrorCode;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.impl.jpa.ModelConfigurationJPARepo;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelSelectionManagementTest {

    @Mock
    private ModelConfigurationRepo modelConfigurationRepo;

    @Mock
    private ModelConfigurationJPARepo modelConfigurationJPARepo;

    @Mock
    private ModelDataMapper modelDataMapper;

    @InjectMocks
    private ModelSelectionManagement modelSelectionService;

    private ModelConfigurationEntity modelEntity1;
    private ModelConfigurationEntity modelEntity2;
    private ModelMinimalResponseDto minimalResponseDto1;
    private ModelMinimalResponseDto minimalResponseDto2;
    private ModelResponseDto responseDto;
    private ModelInfo modelInfo;

    @BeforeEach
    void setUp() {
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

        // Setup test DTOs
        minimalResponseDto1 = ModelMinimalResponseDto.builder()
                .modelId("1")
                .displayName("GPT-4")
                .isEnabled(true)
                .provider("OpenAI")
                .build();

        minimalResponseDto2 = ModelMinimalResponseDto.builder()
                .modelId("2")
                .displayName("Claude 3")
                .isEnabled(false)
                .provider("Anthropic")
                .build();

        responseDto = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .isEnabled(true)
                .isDefault(false)
                .build();

        // Setup test ModelInfo
        modelInfo = new ModelInfo();
        modelInfo.setModelName("new-model");
        modelInfo.setDisplayName("New Model");
        modelInfo.setProvider("test-provider");
        modelInfo.setDefaultModel(true);
    }

    @Test
    @DisplayName("Should return list of model configurations")
    void getModelConfigurations() {
        // Given
        List<ModelConfigurationEntity> entities = Arrays.asList(modelEntity1, modelEntity2);
        List<ModelMinimalResponseDto> expectedDtos = Arrays.asList(minimalResponseDto1, minimalResponseDto2);

        when(modelConfigurationRepo.getModels()).thenReturn(entities);
        when(modelDataMapper.toModelMinimalResponseDto(modelEntity1)).thenReturn(minimalResponseDto1);
        when(modelDataMapper.toModelMinimalResponseDto(modelEntity2)).thenReturn(minimalResponseDto2);

        // When
        List<ModelMinimalResponseDto> result = modelSelectionService.getModelConfigurations();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDtos, result);
        verify(modelConfigurationRepo).getModels();
        verify(modelDataMapper, times(2)).toModelMinimalResponseDto(any(ModelConfigurationEntity.class));
    }

    @Test
    @DisplayName("Should return model configuration for valid model id")
    void getModelConfiguration_ValidModelId() {
        // Given
        Integer modelId = 1;
        // when(modelConfigurationRepo.existsByModelId(modelId)).thenReturn(true);
        when(modelConfigurationRepo.getModelById(modelId)).thenReturn(modelEntity1);
        when(modelDataMapper.toModelResponseDto(modelEntity1)).thenReturn(responseDto);

        // When
        ModelResponseDto result = modelSelectionService.getModelConfiguration(modelId);

        // Then
        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(modelConfigurationRepo).getModelById(modelId);
        verify(modelDataMapper).toModelResponseDto(modelEntity1);
    }

    @Test
    @DisplayName("Should throw AppException when model not found for getModelConfiguration")
    void getModelConfiguration_ModelNotFound() {
        // Given
        Integer nonExistentModelId = 999;
        when(modelConfigurationRepo.getModelById(nonExistentModelId))
                .thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.getModelConfiguration(nonExistentModelId));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
        verify(modelConfigurationRepo, never()).getModelByName(anyString());
        verify(modelDataMapper, never()).toModelResponseDto(any());
    }

    @Test
    @DisplayName("Should set model enabled for valid model id")
    void setModelEnabled_ValidModelId() {
        // Given
        Integer modelId = 1;
        boolean isEnabled = true;

        // When
        assertDoesNotThrow(() -> modelSelectionService.setModelEnabled(modelId, isEnabled));

        // Then
        verify(modelConfigurationRepo).setEnabled(modelId, isEnabled);
    }

    @Test
    @DisplayName("Should set model disabled for non-default model")
    void setModelEnabled_DisableNonDefaultModel() {
        // Given
        Integer modelId = 1;
        boolean isEnabled = false;

        // When
        assertDoesNotThrow(() -> modelSelectionService.setModelEnabled(modelId, isEnabled));

        // Then
        verify(modelConfigurationRepo).setEnabled(modelId, isEnabled);
    }

    @Test
    @DisplayName("Should throw AppException when trying to disable default model")
    void setModelEnabled_DisableDefaultModel() {
        // Given
        Integer modelId = 1;
        boolean isEnabled = false;

        doThrow(new AppException(ErrorCode.INVALID_MODEL_STATUS,
                "Cannot disable the default model. Please set another model as default first."))
                .when(modelConfigurationRepo)
                .setEnabled(modelId, isEnabled);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelEnabled(modelId, isEnabled));

        assertEquals(ErrorCode.INVALID_MODEL_STATUS, exception.getErrorCode());
        verify(modelConfigurationRepo).setEnabled(modelId, isEnabled);
    }

    @Test
    @DisplayName("Should throw AppException when model not found for setModelEnabled")
    void setModelEnabled_ModelNotFound() {
        // Given
        Integer nonExistentModelId = 999;
        boolean isEnabled = true;

        doThrow(new AppException(ErrorCode.MODEL_NOT_FOUND)).when(modelConfigurationRepo)
                .setEnabled(nonExistentModelId, isEnabled);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelEnabled(nonExistentModelId, isEnabled));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
        verify(modelConfigurationRepo).setEnabled(nonExistentModelId, isEnabled);
    }

    @Test
    @DisplayName("Should set enabled model as default and unset other defaults")
    void setDefault_SetEnabledModelAsDefault() {
        // Given
        Integer modelId = 1;
        boolean isDefault = true;

        // When
        assertDoesNotThrow(() -> modelSelectionService.setModelDefault(modelId, isDefault));

        // Then
        verify(modelConfigurationRepo).setDefault(modelId, isDefault);
    }

    @Test
    @DisplayName("Should unset model as default without affecting others")
    void setDefault_UnsetModelAsDefault() {
        // Given
        Integer modelId = 1;
        boolean isDefault = false;

        // When
        assertDoesNotThrow(() -> modelSelectionService.setModelDefault(modelId, isDefault));

        // Then
        verify(modelConfigurationRepo).setDefault(modelId, isDefault);
    }

    @Test
    @DisplayName("Should throw AppException when trying to set disabled model as default")
    void setModelDefault_SetDisabledModelAsDefault() {
        // Given
        Integer modelId = 2;
        boolean isDefault = true;

        doThrow(new AppException(ErrorCode.MODEL_NOT_ENABLED)).when(modelConfigurationRepo)
                .setDefault(modelId, isDefault);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelDefault(modelId, isDefault));

        assertEquals(ErrorCode.MODEL_NOT_ENABLED, exception.getErrorCode());
        verify(modelConfigurationRepo).setDefault(modelId, isDefault);
    }

    @Test
    @DisplayName("Should throw AppException when model not found for setDefault")
    void setModelDefault_ModelNotFound() {
        // Given
        Integer nonExistentModelId = 999;
        boolean isDefault = true;

        doThrow(new AppException(ErrorCode.MODEL_NOT_FOUND)).when(modelConfigurationRepo)
                .setDefault(nonExistentModelId, isDefault);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> modelSelectionService.setModelDefault(nonExistentModelId, isDefault));

        assertEquals(ErrorCode.MODEL_NOT_FOUND, exception.getErrorCode());
        verify(modelConfigurationRepo).setDefault(nonExistentModelId, isDefault);
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

        when(modelDataMapper.toModelConfigurationEntity(modelInfo)).thenReturn(mappedEntity);

        // When
        assertDoesNotThrow(() -> modelSelectionService.saveModelInfo(modelInfo));

        // Then
        verify(modelDataMapper).toModelConfigurationEntity(modelInfo);
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

        when(modelDataMapper.toModelConfigurationEntity(modelInfo)).thenReturn(mappedEntity);

        // When
        assertDoesNotThrow(() -> modelSelectionService.saveModelInfo(modelInfo));

        // Then
        verify(modelDataMapper).toModelConfigurationEntity(modelInfo);
        verify(modelConfigurationRepo).save(argThat(entity -> entity.isEnabled() && !entity.isDefault()
                && entity.getModelName().equals(modelInfo.getModelName())));
    }
}