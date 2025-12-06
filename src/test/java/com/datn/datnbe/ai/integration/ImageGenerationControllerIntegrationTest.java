package com.datn.datnbe.ai.integration;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.entity.Media;
import com.datn.datnbe.document.entity.Presentation;
import com.datn.datnbe.document.entity.valueobject.Slide;
import com.datn.datnbe.document.entity.valueobject.SlideElement;
import com.datn.datnbe.document.enums.SlideElementType;
import com.datn.datnbe.document.repository.MediaRepository;
import com.datn.datnbe.document.repository.PresentationRepository;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyStatus;
import com.datn.datnbe.sharedkernel.idempotency.internal.IdempotencyRepository;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class})
@DisplayName("ImageGenerationController Integration Tests")
public class ImageGenerationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(AIApiClient.class)
    private AIApiClient aiApiClient;

    @MockBean(PresentationApi.class)
    private PresentationApi presentationApi;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private ModelConfigurationRepo modelConfigurationRepo;

    @Autowired
    private PresentationRepository presentationRepository;

    @Autowired
    private MediaRepository mediaRepository;

    private MockMvc mockMvc;
    private ModelConfigurationEntity testImageModel;
    private Presentation testPresentation;
    private static final String BASE64_IMAGE_SAMPLE = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        idempotencyRepository.deleteAll();
        mediaRepository.deleteAll();
        presentationRepository.deleteAll();

        // Create test image model configuration
        testImageModel = new ModelConfigurationEntity();
        testImageModel.setModelName("dall-e-3");
        testImageModel.setDisplayName("DALL-E 3");
        testImageModel.setProvider("OpenAI");
        testImageModel.setModelType(ModelType.IMAGE);
        testImageModel.setEnabled(true);
        testImageModel.setDefault(true);
        modelConfigurationRepo.save(testImageModel);

        // Create test presentation with slide and element
        testPresentation = createTestPresentation();

        // Mock presentationApi.insertImageToPresentation to return success (1)
        when(presentationApi
                .insertImageToPresentation(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(1L);
    }

    private Presentation createTestPresentation() {
        Presentation presentation = new Presentation();
        // Let Hibernate generate the UUID, don't set it manually
        presentation.setTitle("Test Presentation");
        presentation.setCreatedAt(LocalDateTime.now());
        presentation.setUpdatedAt(LocalDateTime.now());

        SlideElement element = new SlideElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(SlideElementType.IMAGE);
        element.setLeft(100.0f);
        element.setTop(100.0f);
        element.setWidth(200.0f);
        element.setHeight(200.0f);

        Slide slide = new Slide();
        slide.setId(UUID.randomUUID().toString());
        slide.setElements(List.of(element));

        presentation.setSlides(List.of(slide));

        return presentationRepository.save(presentation);
    }

    @Test
    @DisplayName("Should successfully generate images without idempotency")
    void generateImage_WithValidRequest_ShouldReturnGeneratedImages() throws Exception {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("A beautiful sunset over the ocean")
                .model("dall-e-3")
                .provider("OpenAI")
                .aspectRatio("16:9")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Arrays.asList(BASE64_IMAGE_SAMPLE, BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/images/generate").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.images").isArray())
                .andExpect(jsonPath("$.data.images", hasSize(2)));

        // Verify AI API was called
        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));

        // Verify media was stored
        List<Media> storedMedia = mediaRepository.findAll();
        assertThat(storedMedia).hasSize(2);
    }

    @Test
    @DisplayName("Should handle generation error gracefully")
    void generateImage_WithApiError_ShouldReturnError() throws Exception {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Invalid prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto errorResponse = new ImageGeneratedResponseDto();
        errorResponse.setImages(null);
        errorResponse.setError("Invalid prompt format");

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/images/generate").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is5xxServerError()); // GENERATION_ERROR maps to 500

        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should reject request with disabled model")
    void generateImage_WithDisabledModel_ShouldReturnError() throws Exception {
        // Given - disable the model
        testImageModel.setEnabled(false);
        modelConfigurationRepo.save(testImageModel);

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        // When & Then
        mockMvc.perform(post("/api/images/generate").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is4xxClientError());

        // AI API should not be called if model is disabled
        verify(aiApiClient, never()).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should successfully generate image with idempotency key")
    void generateImageWithIdempotency_WithValidRequest_ShouldSucceed() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("A beautiful landscape")
                .model("dall-e-3")
                .provider("OpenAI")
                .aspectRatio("1:1")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When - First request
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // Then - Verify idempotency record was created
        Optional<IdempotencyKey> idempotencyRecord = idempotencyRepository.findById(idempotencyKey + "-image");
        assertThat(idempotencyRecord).isPresent();
        assertThat(idempotencyRecord.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(idempotencyRecord.get().getRetryCount()).isZero();

        // Verify AI API was called once
        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));

        // Verify media was stored
        List<Media> storedMedia = mediaRepository.findAll();
        assertThat(storedMedia).hasSize(1);
    }

    @Test
    @DisplayName("Should return cached response for duplicate idempotent request")
    void generateImageWithIdempotency_WithDuplicateRequest_ShouldReturnCached() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("A mountain landscape")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // Reset mock to clear any previous invocations
        clearInvocations(aiApiClient);

        // When - First request
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // When - Second request with same idempotency key
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // Then - Verify AI API was called only once (cached for second request)
        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));

        // Verify only one idempotency record exists
        List<IdempotencyKey> records = idempotencyRepository.findAll();
        long matchingRecords = records.stream()
                .filter(record -> (idempotencyKey + "-image").equals(record.getKey()))
                .count();
        assertThat(matchingRecords).isEqualTo(1);

        // Verify media was stored only once
        List<Media> storedMedia = mediaRepository.findAll();
        assertThat(storedMedia).hasSize(1);
    }

    @Test
    @DisplayName("Should handle multiple different idempotent requests independently")
    void generateImageWithIdempotency_WithDifferentKeys_ShouldHandleIndependently() throws Exception {
        // Given - Create another presentation for second request
        Presentation presentation2 = createTestPresentation();

        String slideId1 = testPresentation.getSlides().get(0).getId();
        String elementId1 = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey1 = testPresentation.getId() + ":" + slideId1 + ":" + elementId1;

        String slideId2 = presentation2.getSlides().get(0).getId();
        String elementId2 = presentation2.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey2 = presentation2.getId() + ":" + slideId2 + ":" + elementId2;

        ImagePromptRequest request1 = ImagePromptRequest.builder()
                .prompt("A sunset")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImagePromptRequest request2 = ImagePromptRequest.builder()
                .prompt("A mountain")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When - First request
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey1)
                .content(objectMapper.writeValueAsString(request1))).andExpect(status().isOk());

        // When - Second request with different key
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey2)
                .content(objectMapper.writeValueAsString(request2))).andExpect(status().isOk());

        // Then - Verify both records exist
        Optional<IdempotencyKey> record1 = idempotencyRepository.findById(idempotencyKey1 + "-image");
        Optional<IdempotencyKey> record2 = idempotencyRepository.findById(idempotencyKey2 + "-image");

        assertThat(record1).isPresent();
        assertThat(record2).isPresent();
        assertThat(record1.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(record2.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);

        // Verify AI API was called twice (once for each unique key)
        verify(aiApiClient, times(2)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));

        // Verify both media were stored
        List<Media> storedMedia = mediaRepository.findAll();
        assertThat(storedMedia).hasSize(2);
    }

    @Test
    @DisplayName("Should reject idempotent request with invalid idempotency key format")
    void generateImageWithIdempotency_WithInvalidKeyFormat_ShouldReturnError() throws Exception {
        // Given
        String invalidIdempotencyKey = "invalid-key-format"; // Missing colon separators

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        // When & Then
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", invalidIdempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is4xxClientError());

        // Verify AI API was not called
        verify(aiApiClient, never()).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should reject idempotent request without idempotency key header")
    void generateImageWithIdempotency_WithoutIdempotencyKey_ShouldReturnError() throws Exception {
        // Given
        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        // When & Then - Request without Idempotency-Key header
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is4xxClientError());

        // Verify AI API was not called
        verify(aiApiClient, never()).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should handle idempotent request with generation error")
    void generateImageWithIdempotency_WithGenerationError_ShouldMarkAsFailed() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Invalid content")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto errorResponse = new ImageGeneratedResponseDto();
        errorResponse.setImages(null);
        errorResponse.setError("Content policy violation");

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is5xxServerError()); // GENERATION_ERROR maps to 500

        // Verify idempotency record was created with failed status
        Optional<IdempotencyKey> idempotencyRecord = idempotencyRepository.findById(idempotencyKey + "-image");
        assertThat(idempotencyRecord).isPresent();
        assertThat(idempotencyRecord.get().getStatus()).isEqualTo(IdempotencyStatus.FAILED);

        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should handle idempotent request when no images are generated")
    void generateImageWithIdempotency_WithEmptyImages_ShouldReturnError() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto emptyResponse = new ImageGeneratedResponseDto();
        emptyResponse.setImages(Collections.emptyList()); // Empty list
        emptyResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is5xxServerError()); // GENERATION_ERROR maps to 500

        verify(aiApiClient, times(1)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should allow retry for failed idempotent request within retry limit")
    void generateImageWithIdempotency_RetryFailedRequest_ShouldSucceed() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String idempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        // First call fails
        ImageGeneratedResponseDto errorResponse = new ImageGeneratedResponseDto();
        errorResponse.setImages(null);
        errorResponse.setError("Temporary error");

        // Second call succeeds
        ImageGeneratedResponseDto successResponse = new ImageGeneratedResponseDto();
        successResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        successResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(errorResponse)
                .thenReturn(errorResponse) // Second attempt also fails
                .thenReturn(successResponse);

        // When - First request (fails)
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is5xxServerError());

        // Verify failed state
        Optional<IdempotencyKey> failedRecord = idempotencyRepository.findById(idempotencyKey + "-image");
        assertThat(failedRecord).isPresent();
        assertThat(failedRecord.get().getStatus()).isEqualTo(IdempotencyStatus.FAILED);
        assertThat(failedRecord.get().getRetryCount()).isEqualTo(0);

        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().is5xxServerError());

        failedRecord = idempotencyRepository.findById(idempotencyKey + "-image");
        assertThat(failedRecord).isPresent();
        assertThat(failedRecord.get().getStatus()).isEqualTo(IdempotencyStatus.FAILED);
        assertThat(failedRecord.get().getRetryCount()).isEqualTo(1);

        // When - Retry request (succeeds)
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // Then - Verify success state
        Optional<IdempotencyKey> successRecord = idempotencyRepository.findById(idempotencyKey + "-image");
        assertThat(successRecord).isPresent();
        assertThat(successRecord.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(successRecord.get().getRetryCount()).isEqualTo(2);

        // Verify AI API was called twice
        verify(aiApiClient, times(3)).post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class));
    }

    @Test
    @DisplayName("Should use correct idempotency key suffix for image generation")
    void generateImageWithIdempotency_ShouldUseImageSuffix() throws Exception {
        // Given
        String slideId = testPresentation.getSlides().get(0).getId();
        String elementId = testPresentation.getSlides().get(0).getElements().get(0).getId();
        String baseIdempotencyKey = testPresentation.getId() + ":" + slideId + ":" + elementId;
        String expectedKey = baseIdempotencyKey + "-image";

        ImagePromptRequest request = ImagePromptRequest.builder()
                .prompt("Test prompt")
                .model("dall-e-3")
                .provider("OpenAI")
                .build();

        ImageGeneratedResponseDto mockResponse = new ImageGeneratedResponseDto();
        mockResponse.setImages(Collections.singletonList(BASE64_IMAGE_SAMPLE));
        mockResponse.setError(null);

        when(aiApiClient.post(any(String.class), any(Map.class), eq(ImageGeneratedResponseDto.class)))
                .thenReturn(mockResponse);

        // When
        mockMvc.perform(post("/api/images/generate-in-presentation").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", baseIdempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // Then - Verify the key has -image suffix
        Optional<IdempotencyKey> record = idempotencyRepository.findById(expectedKey);
        assertThat(record).isPresent();
        assertThat(record.get().getKey()).isEqualTo(expectedKey);

        // Verify base key without suffix does not exist
        Optional<IdempotencyKey> baseRecord = idempotencyRepository.findById(baseIdempotencyKey);
        assertThat(baseRecord).isNotPresent();
    }
}
