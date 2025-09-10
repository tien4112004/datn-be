package com.datn.datnbe.ai.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.mapper.ModelDataMapper;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModelSelectionManagement Text and Image Model Tests")
class ModelSelectionManagementTextImageTest {

    @Mock
    private ModelConfigurationRepo modelConfigurationRepo;

    private ModelDataMapper modelDataMapper;
    private ModelSelectionManagement modelSelectionManagement;

    private ModelConfigurationEntity textModel1;
    private ModelConfigurationEntity textModel2;
    private ModelConfigurationEntity imageModel1;
    private ModelConfigurationEntity imageModel2;

    @BeforeEach
    void setUp() {
        modelDataMapper = Mappers.getMapper(ModelDataMapper.class);
        modelSelectionManagement = new ModelSelectionManagement(modelConfigurationRepo, modelDataMapper);

        // Setup text models
        textModel1 = new ModelConfigurationEntity();
        textModel1.setModelId(1);
        textModel1.setModelName("gpt-4");
        textModel1.setDisplayName("GPT-4");
        textModel1.setProvider("openai");
        textModel1.setEnabled(true);
        textModel1.setDefault(true);
        textModel1.setTextCapable(true);
        textModel1.setImageCapable(false);

        textModel2 = new ModelConfigurationEntity();
        textModel2.setModelId(2);
        textModel2.setModelName("claude-3");
        textModel2.setDisplayName("Claude 3");
        textModel2.setProvider("anthropic");
        textModel2.setEnabled(true);
        textModel2.setDefault(false);
        textModel2.setTextCapable(true);
        textModel2.setImageCapable(false);

        // Setup image models
        imageModel1 = new ModelConfigurationEntity();
        imageModel1.setModelId(10);
        imageModel1.setModelName("dall-e-3");
        imageModel1.setDisplayName("DALL-E 3");
        imageModel1.setProvider("openai");
        imageModel1.setEnabled(true);
        imageModel1.setDefault(true);
        imageModel1.setTextCapable(false);
        imageModel1.setImageCapable(true);

        imageModel2 = new ModelConfigurationEntity();
        imageModel2.setModelId(11);
        imageModel2.setModelName("midjourney");
        imageModel2.setDisplayName("Midjourney");
        imageModel2.setProvider("midjourney");
        imageModel2.setEnabled(false);
        imageModel2.setDefault(false);
        imageModel2.setTextCapable(false);
        imageModel2.setImageCapable(true);
    }

    // ===============================
    // Tests for getTextModelModelConfigurations
    // ===============================

    @Test
    @DisplayName("Should return sorted list of text model configurations")
    void getTextModelModelConfigurations_WithValidData_ShouldReturnSortedList() {
        // Given
        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1, textModel2);
        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getTextModelModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify sorting by provider (anthropic comes before openai)
        assertThat(result.get(0).getProvider()).isEqualTo("anthropic");
        assertThat(result.get(0).getModelName()).isEqualTo("claude-3");
        assertThat(result.get(0).getDisplayName()).isEqualTo("Claude 3");
        assertThat(result.get(0).isEnabled()).isTrue();
        assertThat(result.get(0).isDefault()).isFalse();

        assertThat(result.get(1).getProvider()).isEqualTo("openai");
        assertThat(result.get(1).getModelName()).isEqualTo("gpt-4");
        assertThat(result.get(1).getDisplayName()).isEqualTo("GPT-4");
        assertThat(result.get(1).isEnabled()).isTrue();
        assertThat(result.get(1).isDefault()).isTrue();
    }

    @Test
    @DisplayName("Should return empty list when no text models exist")
    void getTextModelModelConfigurations_WithNoTextModels_ShouldReturnEmptyList() {
        // Given
        when(modelConfigurationRepo.getTextModels()).thenReturn(Collections.emptyList());

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getTextModelModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return single text model configuration")
    void getTextModelModelConfigurations_WithSingleTextModel_ShouldReturnSingleItem() {
        // Given
        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1);
        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getTextModelModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelName()).isEqualTo("gpt-4");
        assertThat(result.get(0).getDisplayName()).isEqualTo("GPT-4");
        assertThat(result.get(0).getProvider()).isEqualTo("openai");
        assertThat(result.get(0).isEnabled()).isTrue();
        assertThat(result.get(0).isDefault()).isTrue();
    }

    @Test
    @DisplayName("Should handle text models with same provider (sorted by model name)")
    void getTextModelModelConfigurations_WithSameProvider_ShouldSortByProvider() {
        // Given
        ModelConfigurationEntity textModel3 = new ModelConfigurationEntity();
        textModel3.setModelId(3);
        textModel3.setModelName("gpt-3.5");
        textModel3.setDisplayName("GPT-3.5");
        textModel3.setProvider("openai");
        textModel3.setEnabled(true);
        textModel3.setDefault(false);
        textModel3.setTextCapable(true);

        List<ModelConfigurationEntity> textModels = Arrays.asList(textModel1, textModel3);
        when(modelConfigurationRepo.getTextModels()).thenReturn(textModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getTextModelModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        // Both should have same provider (sorted by provider, so order might vary)
        assertThat(result.get(0).getProvider()).isEqualTo("openai");
        assertThat(result.get(1).getProvider()).isEqualTo("openai");
    }

    @Test
    @DisplayName("Should handle repository exception for text models")
    void getTextModelModelConfigurations_WithRepositoryException_ShouldThrowException() {
        // Given
        when(modelConfigurationRepo.getTextModels()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> modelSelectionManagement.getTextModelModelConfigurations())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
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
        List<ModelResponseDto> result = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify sorting by provider (midjourney comes before openai)
        assertThat(result.get(0).getProvider()).isEqualTo("midjourney");
        assertThat(result.get(0).getModelName()).isEqualTo("midjourney");
        assertThat(result.get(0).getDisplayName()).isEqualTo("Midjourney");
        assertThat(result.get(0).isEnabled()).isFalse();
        assertThat(result.get(0).isDefault()).isFalse();

        assertThat(result.get(1).getProvider()).isEqualTo("openai");
        assertThat(result.get(1).getModelName()).isEqualTo("dall-e-3");
        assertThat(result.get(1).getDisplayName()).isEqualTo("DALL-E 3");
        assertThat(result.get(1).isEnabled()).isTrue();
        assertThat(result.get(1).isDefault()).isTrue();
    }

    @Test
    @DisplayName("Should return empty list when no image models exist")
    void getImageModelConfigurations_WithNoImageModels_ShouldReturnEmptyList() {
        // Given
        when(modelConfigurationRepo.getImageModels()).thenReturn(Collections.emptyList());

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return single image model configuration")
    void getImageModelConfigurations_WithSingleImageModel_ShouldReturnSingleItem() {
        // Given
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel1);
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelName()).isEqualTo("dall-e-3");
        assertThat(result.get(0).getDisplayName()).isEqualTo("DALL-E 3");
        assertThat(result.get(0).getProvider()).isEqualTo("openai");
        assertThat(result.get(0).isEnabled()).isTrue();
        assertThat(result.get(0).isDefault()).isTrue();
    }

    @Test
    @DisplayName("Should handle image models with mixed capabilities")
    void getImageModelConfigurations_WithMixedCapabilities_ShouldReturnOnlyImageModels() {
        // Given
        ModelConfigurationEntity multiCapableModel = new ModelConfigurationEntity();
        multiCapableModel.setModelId(20);
        multiCapableModel.setModelName("gpt-4-vision");
        multiCapableModel.setDisplayName("GPT-4 Vision");
        multiCapableModel.setProvider("openai");
        multiCapableModel.setEnabled(true);
        multiCapableModel.setDefault(false);
        multiCapableModel.setTextCapable(true);
        multiCapableModel.setImageCapable(true);

        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel1, multiCapableModel);
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        // Both should be returned as they are image capable
        assertThat(result.stream().map(ModelResponseDto::getModelName)).containsExactlyInAnyOrder("dall-e-3",
                "gpt-4-vision");
    }

    @Test
    @DisplayName("Should handle disabled image models")
    void getImageModelConfigurations_WithDisabledModels_ShouldIncludeDisabledModels() {
        // Given
        List<ModelConfigurationEntity> imageModels = Arrays.asList(imageModel2); // imageModel2 is disabled
        when(modelConfigurationRepo.getImageModels()).thenReturn(imageModels);

        // When
        List<ModelResponseDto> result = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelName()).isEqualTo("midjourney");
        assertThat(result.get(0).isEnabled()).isFalse();
        assertThat(result.get(0).isDefault()).isFalse();
    }

    @Test
    @DisplayName("Should handle repository exception for image models")
    void getImageModelConfigurations_WithRepositoryException_ShouldThrowException() {
        // Given
        when(modelConfigurationRepo.getImageModels()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> modelSelectionManagement.getImageModelConfigurations())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
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
        List<ModelResponseDto> textResult = modelSelectionManagement.getTextModelModelConfigurations();
        List<ModelResponseDto> imageResult = modelSelectionManagement.getImageModelConfigurations();

        // Then
        assertThat(textResult).hasSize(2);
        assertThat(imageResult).hasSize(2);

        // Verify they contain different models
        assertThat(textResult.stream().map(ModelResponseDto::getModelName)).containsExactlyInAnyOrder("gpt-4",
                "claude-3");
        assertThat(imageResult.stream().map(ModelResponseDto::getModelName)).containsExactlyInAnyOrder("dall-e-3",
                "midjourney");

        // Ensure no overlap
        assertThat(textResult.stream().map(ModelResponseDto::getModelName))
                .doesNotContainAnyElementsOf(imageResult.stream().map(ModelResponseDto::getModelName).toList());
    }

    @Test
    @DisplayName("Should handle null repository responses gracefully")
    void getModelConfigurations_WithNullRepositoryResponse_ShouldHandleGracefully() {
        // Given
        when(modelConfigurationRepo.getTextModels()).thenReturn(null);
        when(modelConfigurationRepo.getImageModels()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> modelSelectionManagement.getTextModelModelConfigurations())
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> modelSelectionManagement.getImageModelConfigurations())
                .isInstanceOf(NullPointerException.class);
    }
}
