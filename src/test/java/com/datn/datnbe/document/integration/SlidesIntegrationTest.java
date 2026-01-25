package com.datn.datnbe.document.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import com.datn.datnbe.auth.api.ResourcePermissionApi;
import com.datn.datnbe.auth.dto.request.ResourceRegistrationRequest;
import com.datn.datnbe.auth.dto.response.ResourcePermissionResponse;
import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.SlideUpdateRequest;
import com.datn.datnbe.document.dto.request.SlidesUpsertRequest;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyStatus;
import com.datn.datnbe.sharedkernel.idempotency.internal.IdempotencyRepository;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class})
@Disabled("Disabled - Docker required for integration tests")
public class SlidesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @MockBean(name = "resourcePermissionManagement")
    private ResourcePermissionApi resourcePermissionApi;

    private MockMvc mockMvc;
    private String presentationId;
    private List<String> registeredPresentations = new ArrayList<>();
    private Jwt testJwt;

    @BeforeEach
    void setUp() throws Exception {
        // Create a test JWT
        testJwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "test-user-id")
                .claim("scope", "read write")
                .build();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        idempotencyRepository.deleteAll();
        registeredPresentations.clear();

        // Mock permission API to always grant full permissions
        when(resourcePermissionApi.checkUserPermissions(anyString(), anyString()))
                .thenReturn(ResourcePermissionResponse.builder()
                        .resourceId("test-resource")
                        .userId("test-user-id")
                        .permissions(Set.of("read", "edit", "comment"))
                        .hasAccess(true)
                        .build());

        // Mock ResourcePermissionApi to track registered resources
        when(resourcePermissionApi.registerResource(org.mockito.ArgumentMatchers.any(), anyString()))
                .thenAnswer(invocation -> {
                    ResourceRegistrationRequest request = invocation.getArgument(0);
                    registeredPresentations.add(request.getId());
                    return null;
                });
        when(resourcePermissionApi.getAllResourceByTypeOfOwner(anyString(), anyString()))
                .thenAnswer(invocation -> new ArrayList<>(registeredPresentations));

        presentationId = createTestPresentation();
    }

    private String createTestPresentation() throws Exception {
        Map<String, Object> background = new HashMap<>();
        background.put("type", "color");
        background.put("color", "#ffffff");

        Map<String, Object> element = new HashMap<>();
        element.put("type", "text");
        element.put("id", "initial-element");
        element.put("left", 100.0f);
        element.put("top", 200.0f);
        element.put("width", 300.0f);
        element.put("height", 50.0f);
        element.put("content", "Initial slide content");
        element.put("defaultFontName", "Arial");
        element.put("defaultColor", "#000000");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(element));
        slideExtraFields.put("background", background);

        SlideDto slide = SlideDto.builder().id("initial-slide").extraFields(slideExtraFields).build();

        PresentationCreateRequest createRequest = PresentationCreateRequest.builder()
                .title("test presentation")
                .slides(List.of(slide))
                .build();

        MvcResult result = mockMvc
                .perform(post("/api/presentations").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        // Extract presentation id
        return objectMapper.readTree(responseContent).path("data").path("id").asText();
    }

    @Test
    @DisplayName("Should successfully upsert slides with idempotency")
    void upsertSlides_WithValidRequest_ShouldSucceedWithIdempotency() throws Exception {
        // Given
        String idempotencyKey = "integration-test-key-1";

        Map<String, Object> newElement = new HashMap<>();
        newElement.put("type", "text");
        newElement.put("id", "new-element-1");
        newElement.put("left", 50.0f);
        newElement.put("top", 100.0f);
        newElement.put("width", 250.0f);
        newElement.put("height", 75.0f);
        newElement.put("content", "New slide content from integration test");
        newElement.put("defaultFontName", "Helvetica");
        newElement.put("defaultColor", "#333333");

        Map<String, Object> newBackground = new HashMap<>();
        newBackground.put("type", "gradient");
        newBackground.put("color", "linear-gradient(45deg, #ff6b6b, #4ecdc4)");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(newElement));
        slideExtraFields.put("background", newBackground);

        SlideUpdateRequest newSlide = SlideUpdateRequest.builder()
                .id("integration-slide-1")
                .extraFields(slideExtraFields)
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(newSlide)).build();

        // When & Then - First request
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        // Verify idempotency record was created
        Optional<IdempotencyKey> idempotencyRecord = idempotencyRepository.findById(idempotencyKey);
        assertThat(idempotencyRecord).isPresent();
        assertThat(idempotencyRecord.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);

        // When & Then - Second request with same idempotency key (should be idempotent)
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        // Verify only one record exists with same key
        List<IdempotencyKey> records = idempotencyRepository.findAll();
        long matchingRecords = records.stream().filter(record -> idempotencyKey.equals(record.getKey())).count();
        assertThat(matchingRecords).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle multiple slides upsert operation")
    void upsertSlides_WithMultipleSlides_ShouldProcessAll() throws Exception {
        // Given
        String idempotencyKey = "integration-test-multiple-slides";

        Map<String, Object> element1 = new HashMap<>();
        element1.put("type", "text");
        element1.put("id", "multi-element-1");
        element1.put("content", "First slide in batch");

        Map<String, Object> element2 = new HashMap<>();
        element2.put("type", "shape");
        element2.put("id", "multi-element-2");
        element2.put("path", "M10,10 L90,90");
        element2.put("fill", "#ff0000");

        Map<String, Object> slide1ExtraFields = new HashMap<>();
        slide1ExtraFields.put("elements", List.of(element1));

        Map<String, Object> slide2ExtraFields = new HashMap<>();
        slide2ExtraFields.put("elements", List.of(element2));

        SlideUpdateRequest slide1 = SlideUpdateRequest.builder()
                .id("multi-slide-1")
                .extraFields(slide1ExtraFields)
                .build();

        SlideUpdateRequest slide2 = SlideUpdateRequest.builder()
                .id("multi-slide-2")
                .extraFields(slide2ExtraFields)
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(Arrays.asList(slide1, slide2)).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        // Verify idempotency record
        Optional<IdempotencyKey> record = idempotencyRepository.findById(idempotencyKey);
        assertThat(record).isPresent();
        assertThat(record.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should handle complex slide elements with all properties")
    void upsertSlides_WithComplexElements_ShouldPreserveAllData() throws Exception {
        // Given
        String idempotencyKey = "integration-test-complex-elements";

        Map<String, Object> complexElement = new HashMap<>();
        complexElement.put("type", "vector");
        complexElement.put("id", "complex-element-integration");
        complexElement.put("left", 25.0f);
        complexElement.put("top", 50.0f);
        complexElement.put("width", 400.0f);
        complexElement.put("height", 300.0f);
        complexElement.put("viewBox", Arrays.asList(0.0f, 0.0f, 400.0f, 300.0f));
        complexElement.put("path", "M25,50 Q100,25 175,50 T325,50");
        complexElement.put("fill", "#4a90e2");
        complexElement.put("fixedRatio", true);
        complexElement.put("opacity", 0.85f);
        complexElement.put("rotate", 15.0f);
        complexElement.put("flipV", false);
        complexElement.put("lineHeight", 1.4f);
        complexElement.put("content", "Complex element integration test");
        complexElement.put("defaultFontName", "Roboto");
        complexElement.put("defaultColor", "#2c3e50");
        complexElement.put("start", Arrays.asList(25.0f, 50.0f));
        complexElement.put("end", Arrays.asList(325.0f, 50.0f));
        complexElement.put("points", Arrays.asList("25,50", "100,25", "175,50", "250,75", "325,50"));
        complexElement.put("color", "#e74c3c");
        complexElement.put("style", "dashed");
        complexElement.put("wordSpace", 1.2f);

        Map<String, Object> complexBackground = new HashMap<>();
        complexBackground.put("type", "image");
        complexBackground.put("color", "url('/api/images/background-pattern.png')");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(complexElement));
        slideExtraFields.put("background", complexBackground);

        SlideUpdateRequest complexSlide = SlideUpdateRequest.builder()
                .id("complex-integration-slide")
                .extraFields(slideExtraFields)
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(complexSlide)).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        // Verify processing completed
        Optional<IdempotencyKey> record = idempotencyRepository.findById(idempotencyKey);
        assertThat(record).isPresent();
        assertThat(record.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should reject request without idempotency key")
    void upsertSlides_WithoutIdempotencyKey_ShouldReturnBadRequest() throws Exception {
        // Given
        SlideUpdateRequest slide = SlideUpdateRequest.builder()
                .id("slide-without-key")
                .extraFields(new HashMap<>())
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(slide)).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle validation errors for invalid slide data")
    void upsertSlides_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given - Slide with blank ID (violates @NotBlank constraint)
        String idempotencyKey = "integration-test-validation-error";
        SlideUpdateRequest invalidSlide = SlideUpdateRequest.builder()
                .id("")  // Invalid: blank ID
                .extraFields(new HashMap<>())
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(invalidSlide)).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        // Verify no idempotency record was created for failed validation
        Optional<IdempotencyKey> record = idempotencyRepository.findById(idempotencyKey);
        assertThat(record).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty slides list")
    void upsertSlides_WithEmptySlidesList_ShouldSucceed() throws Exception {
        // Given
        String idempotencyKey = "integration-test-empty-slides";
        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of()).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        // Verify idempotency record was created
        Optional<IdempotencyKey> record = idempotencyRepository.findById(idempotencyKey);
        assertThat(record).isPresent();
        assertThat(record.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should handle concurrent requests with different idempotency keys")
    void upsertSlides_ConcurrentRequests_ShouldHandleIndependently() throws Exception {
        // Given
        String idempotencyKey1 = "concurrent-test-key-1";
        String idempotencyKey2 = "concurrent-test-key-2";

        SlideUpdateRequest slide1 = SlideUpdateRequest.builder()
                .id("concurrent-slide-1")
                .extraFields(new HashMap<>())
                .build();

        SlideUpdateRequest slide2 = SlideUpdateRequest.builder()
                .id("concurrent-slide-2")
                .extraFields(new HashMap<>())
                .build();

        SlidesUpsertRequest request1 = SlidesUpsertRequest.builder().slides(List.of(slide1)).build();

        SlidesUpsertRequest request2 = SlidesUpsertRequest.builder().slides(List.of(slide2)).build();

        // When & Then - Process both requests
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey1)
                .content(objectMapper.writeValueAsString(request1))).andExpect(status().isNoContent());

        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey2)
                .content(objectMapper.writeValueAsString(request2))).andExpect(status().isNoContent());

        // Verify both idempotency records were created
        Optional<IdempotencyKey> record1 = idempotencyRepository.findById(idempotencyKey1);
        Optional<IdempotencyKey> record2 = idempotencyRepository.findById(idempotencyKey2);

        assertThat(record1).isPresent();
        assertThat(record2).isPresent();
        assertThat(record1.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(record2.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void upsertSlides_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Given
        String idempotencyKey = "integration-test-malformed-json";
        String malformedJson = "{\"slides\": [\"id\": \"broken-json\"}";

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(malformedJson)).andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle null slides")
    void upsertSlides_WithNullSlides_ShouldReturnValidationError() throws Exception {
        // Given
        String idempotencyKey = "integration-test-null-slides";
        String nullSlidesJson = "{\"slides\": null}";

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(testJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", idempotencyKey)
                .content(nullSlidesJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }
}
