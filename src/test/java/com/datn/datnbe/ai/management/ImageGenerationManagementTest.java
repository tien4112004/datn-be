package com.datn.datnbe.ai.management;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageGenerationManagement Unit Tests")
class ImageGenerationManagementTest {

    @Mock
    private AIApiClient aiApiClient;

    @Mock
    private ModelSelectionApi modelSelectionApi;

    @Mock
    private com.datn.datnbe.ai.service.PicsumPhotoService picsumPhotoService;

    private ImageGenerationManagement imageGenerationManagement;

    private static final String IMAGE_API_ENDPOINT = "/api/image/generate";
    private static final String BASE64_IMAGE_SAMPLE = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String BASE64_IMAGE_SAMPLE_2 = "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAYAAABytg0kAAAAEklEQVR42mNk+M9QzwAEjDAGACCKAoFFBJHUAAAAAElFTkSuQmCC";
    private static final String TRACE_ID = "test-trace-id-12345";

    @BeforeEach
    void setUp() {
        imageGenerationManagement = new ImageGenerationManagement(aiApiClient, modelSelectionApi, picsumPhotoService);
        ReflectionTestUtils.setField(imageGenerationManagement, "IMAGE_API_ENDPOINT", IMAGE_API_ENDPOINT);
    }

    @Test
    @DisplayName("Should successfully generate single image when model is enabled")
    void shouldGenerateSingleImageSuccessfully() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("A beautiful sunset over mountains")
                .model("dall-e-3")
                .provider("OpenAI")
                .aspectRatio("16:9")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOriginalFilename()).isEqualTo("AI_generated_image.png");
        assertThat(result.get(0).getContentType()).isEqualTo("image/png");
        assertThat(result.get(0).getName()).isEqualTo("image");

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should successfully generate multiple images")
    void shouldGenerateMultipleImagesSuccessfully() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Abstract art")
                .model("dall-e-3")
                .provider("OpenAI")
                .aspectRatio("1:1")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Arrays.asList(BASE64_IMAGE_SAMPLE, BASE64_IMAGE_SAMPLE_2));
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOriginalFilename()).isEqualTo("AI_generated_image.png");
        assertThat(result.get(1).getOriginalFilename()).isEqualTo("AI_generated_image.png");

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should throw AppException when model is not enabled")
    void shouldThrowExceptionWhenModelNotEnabled() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("A beautiful sunset")
                .model("disabled-model")
                .provider("OpenAI")
                .build();

        when(modelSelectionApi.isModelEnabled("disabled-model")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MODEL_NOT_ENABLED);

        verify(modelSelectionApi, times(1)).isModelEnabled("disabled-model");
        verify(aiApiClient, never()).post(anyString(), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should throw AppException when API returns error")
    void shouldThrowExceptionWhenApiReturnsError() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Invalid prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(null);
        mockResponse.setError("Invalid API key provided");

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_ERROR)
                .hasMessageContaining("Invalid API key provided");

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should throw AppException when API returns empty image list")
    void shouldThrowExceptionWhenApiReturnsEmptyImageList() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.emptyList());
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_ERROR);

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should throw AppException when API returns null images")
    void shouldThrowExceptionWhenApiReturnsNullImages() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(null);
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_ERROR);

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should throw AppException when base64 decoding fails")
    void shouldThrowExceptionWhenBase64DecodingFails() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList("invalid-base64-string!!!"));
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_BASE64_FORMAT);

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should generate image with all request parameters")
    void shouldGenerateImageWithAllParameters() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Detailed landscape")
                .model("stable-diffusion")
                .provider("Stability AI")
                .aspectRatio("4:3")
                .artStyle("oil painting")
                .artDescription("Impressionist style")
                .themeStyle("nature")
                .themeDescription("Peaceful mountain scenery")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("stable-diffusion")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSize()).isGreaterThan(0);

        verify(modelSelectionApi, times(1)).isModelEnabled("stable-diffusion");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should handle API error with null error message")
    void shouldHandleApiErrorWithNullErrorMessage() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(null);
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> imageGenerationManagement.generateImage(request, TRACE_ID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENERATION_ERROR);

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should verify MultipartFile properties after conversion")
    void shouldVerifyMultipartFileProperties() {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test image")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(modelSelectionApi.isModelEnabled("dall-e-3")).thenReturn(true);
        when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class))).thenReturn(mockResponse);

        // When
        List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        MultipartFile file = result.get(0);
        assertThat(file.getOriginalFilename()).isEqualTo("AI_generated_image.png");
        assertThat(file.getContentType()).isEqualTo("image/png");
        assertThat(file.getName()).isEqualTo("image");
        assertThat(file.isEmpty()).isFalse();
        assertThat(file.getSize()).isGreaterThan(0);

        verify(modelSelectionApi, times(1)).isModelEnabled("dall-e-3");
        verify(aiApiClient, times(1)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }

    @Test
    @DisplayName("Should generate images with different models")
    void shouldGenerateImagesWithDifferentModels() {
        // Given
        String[] models = {"dall-e-3", "stable-diffusion", "midjourney"};

        for (String model : models) {
            ImagePromptRequest request = ImagePromptRequest.builder()
                    .prompt("Test prompt")
                    .model(model)
                    .provider("Test Provider")
                    .build();

            ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
            mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
            mockResponse.setError(null);

            when(modelSelectionApi.isModelEnabled(model)).thenReturn(true);
            when(aiApiClient.post(eq(IMAGE_API_ENDPOINT),
                    any(Map.class),
                    eq(ImageGeneratedResponseDto.class),
                    any(HttpHeaders.class))).thenReturn(mockResponse);

            // When
            List<MultipartFile> result = imageGenerationManagement.generateImage(request, TRACE_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }

        verify(modelSelectionApi, times(3)).isModelEnabled(anyString());
        verify(aiApiClient, times(3)).post(eq(IMAGE_API_ENDPOINT),
                any(Map.class),
                eq(ImageGeneratedResponseDto.class),
                any(HttpHeaders.class));
    }
}
