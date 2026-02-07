package com.datn.datnbe.ai.integration;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.management.ImageGenerationManagement;
import com.datn.datnbe.ai.repository.ModelConfigurationRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class})
@DisplayName("ImageGenerationManagement Integration Tests")
@Disabled("Disabled - Docker required for integration tests")
class ImageGenerationManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ImageGenerationManagement imageGenerationManagement;

    @Autowired
    private ModelConfigurationRepository modelConfigurationRepo;

    @MockBean
    private AIApiClient aiApiClient;

    private ModelConfigurationEntity enabledImageModel;
    private ModelConfigurationEntity disabledImageModel;
    private ModelConfigurationEntity textModel;

    private static final String BASE64_IMAGE_SAMPLE = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String BASE64_IMAGE_SAMPLE_2 = "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAEklEQVR42mNk+M9QzwAEjDAGACCKAoFFBJHUAAAAAElFTkSuQmCC";
    private static final String TRACE_ID = "test-trace-id-integration-12345";

    @BeforeEach
    void setUp() {
        // Clean up existing models
        List<ModelConfigurationEntity> existingModels = modelConfigurationRepo.findAll();
        for (ModelConfigurationEntity model : existingModels) {
            modelConfigurationRepo.deleteByModelName(model.getModelName());
        }

        // Create enabled image model
        enabledImageModel = new ModelConfigurationEntity();
        enabledImageModel.setModelName("dall-e-3");
        enabledImageModel.setDisplayName("DALL-E 3");
        enabledImageModel.setProvider("OpenAI");
        enabledImageModel.setModelType(ModelType.IMAGE);
        enabledImageModel.setEnabled(true);
        enabledImageModel.setDefault(true);
        modelConfigurationRepo.save(enabledImageModel);

        // Create disabled image model
        disabledImageModel = new ModelConfigurationEntity();
        disabledImageModel.setModelName("stable-diffusion");
        disabledImageModel.setDisplayName("Stable Diffusion");
        disabledImageModel.setProvider("Stability AI");
        disabledImageModel.setModelType(ModelType.IMAGE);
        disabledImageModel.setEnabled(false);
        disabledImageModel.setDefault(false);
        modelConfigurationRepo.save(disabledImageModel);

        // Create text model (should not interfere with image generation)
        textModel = new ModelConfigurationEntity();
        textModel.setModelName("gpt-4");
        textModel.setDisplayName("GPT-4");
        textModel.setProvider("OpenAI");
        textModel.setModelType(ModelType.TEXT);
        textModel.setEnabled(true);
        textModel.setDefault(true);
        modelConfigurationRepo.save(textModel);
    }

    // ... (tests)

    @Test
    @DisplayName("Should verify model selection logic with database state")
    void shouldVerifyModelSelectionWithDatabaseState() {
        // Given - Model is enabled in database
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test image")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(anyString(), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).isNotNull();

        // Verify database state
        ModelConfigurationEntity model = modelConfigurationRepo.findByModelNameAndModelType("dall-e-3", ModelType.IMAGE)
                .orElseThrow();
        assertThat(model).isNotNull();
        assertThat(model.isEnabled()).isTrue();

        // Now disable the model
        model.setEnabled(false);
        modelConfigurationRepo.save(model);

        // Try to generate again
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MODEL_NOT_ENABLED);
    }

    @Test
    @DisplayName("Should handle concurrent model status changes")
    void shouldHandleConcurrentModelStatusChanges() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(anyString(), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // First generation should succeed
        List<MultipartFile> result1 = imageGenerationManagement.generateImage(request, TRACE_ID);
        assertThat(result1).hasSize(1);

        // Disable model
        ModelConfigurationEntity model = modelConfigurationRepo.findByModelNameAndModelType("dall-e-3", ModelType.IMAGE)
                .orElseThrow();
        model.setEnabled(false);
        modelConfigurationRepo.save(model);

        // Second generation should fail
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MODEL_NOT_ENABLED);

        // Re-enable model
        model.setEnabled(true);
        modelConfigurationRepo.save(model);

        // Third generation should succeed
        List<MultipartFile> result2 = imageGenerationManagement.generateImage(request, TRACE_ID);
        assertThat(result2).hasSize(1);
    }

    @Test
    @DisplayName("Should properly convert base64 to multipart file with correct metadata")
    void shouldConvertBase64ToMultipartFileWithCorrectMetadata() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test image")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(anyString(), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).hasSize(1);
        MultipartFile file = result.get(0);

        // Verify file metadata
        assertThat(file.getOriginalFilename()).isEqualTo("AI_generated_image.png");
        assertThat(file.getContentType()).isEqualTo("image/png");
        assertThat(file.getName()).isEqualTo("image");
        assertThat(file.isEmpty()).isFalse();
        assertThat(file.getSize()).isGreaterThan(0);

        // Verify file content can be read
        try {
            byte[] bytes = file.getBytes();
            assertThat(bytes).isNotNull();
            assertThat(bytes.length).isGreaterThan(0);
        } catch (Exception e) {
            throw new AssertionError("Failed to read file bytes", e);
        }
    }
}
