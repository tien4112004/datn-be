package com.datn.datnbe.document.controller;

import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.api.SlidesApi;
import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.request.SlideUpdateRequest;
import com.datn.datnbe.document.dto.request.SlidesUpsertRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.presentation.PresentationController;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = PresentationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
class PresentationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresentationApi presentationApi;

    @MockitoBean
    private SlidesApi slidesApi;

    @Autowired
    private ObjectMapper objectMapper;

    private PresentationCreateRequest request;
    private PresentationCreateResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        Map<String, Object> background = new HashMap<>();
        background.put("type", "color");
        background.put("color", "#ffffff");

        Map<String, Object> element = new HashMap<>();
        element.put("type", "text");
        element.put("id", "element-1");
        element.put("left", 100.0f);
        element.put("top", 200.0f);
        element.put("width", 300.0f);
        element.put("height", 50.0f);
        element.put("content", "Sample text");
        element.put("defaultFontName", "Arial");
        element.put("defaultColor", "#000000");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(element));
        slideExtraFields.put("background", background);

        SlideDto slide = SlideDto.builder().id("slide-1").extraFields(slideExtraFields).build();

        request = PresentationCreateRequest.builder().slides(List.of(slide)).build();

        mockResponse = PresentationCreateResponseDto.builder()
                .title("Test Presentation")
                .slides(List.of(slide))
                .build();
    }

    @Test
	void createPresentation_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
		when(presentationApi.createPresentation(any(PresentationCreateRequest.class)))
				.thenReturn(mockResponse);

		mockMvc.perform(post("/api/presentations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data.title").value("Test Presentation"))
				.andExpect(jsonPath("$.data.slides").isArray())
				.andExpect(jsonPath("$.data.slides[0].id").value("slide-1"))
				.andExpect(jsonPath("$.data.slides[0].elements").isArray())
				.andExpect(jsonPath("$.data.slides[0].elements[0].type").value("text"))
				.andExpect(jsonPath("$.data.slides[0].elements[0].id").value("element-1"))
				.andExpect(jsonPath("$.data.slides[0].elements[0].content").value("Sample text"))
				.andExpect(jsonPath("$.data.slides[0].background.type").value("color"))
				.andExpect(jsonPath("$.data.slides[0].background.color").value("#ffffff"));
	}

    @Test
    void createPresentation_WithMultipleSlides_ShouldReturnAllSlides() throws Exception {
        SlideDto secondSlide = SlideDto.builder()
                .id("slide-2")
                .extraFields(request.getSlides().get(0).getExtraFields())
                .build();

        request = PresentationCreateRequest.builder()
                .slides(Arrays.asList(request.getSlides().get(0), secondSlide))
                .build();
        mockResponse = PresentationCreateResponseDto.builder()
                .title("Test Presentation")
                .slides(Arrays.asList(request.getSlides().get(0), secondSlide))
                .build();

        when(presentationApi.createPresentation(any(PresentationCreateRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slides").isArray())
                .andExpect(jsonPath("$.data.slides.length()").value(2))
                .andExpect(jsonPath("$.data.slides[0].id").value("slide-1"))
                .andExpect(jsonPath("$.data.slides[1].id").value("slide-2"));
    }

    @Test
    void createPresentation_WithComplexElements_ShouldPreserveAllProperties() throws Exception {
        Map<String, Object> complexElement = new HashMap<>();
        complexElement.put("type", "text");
        complexElement.put("id", "complex-element");
        complexElement.put("left", 50.0f);
        complexElement.put("top", 75.0f);
        complexElement.put("width", 200.0f);
        complexElement.put("height", 150.0f);
        complexElement.put("viewBox", Arrays.asList(0.0f, 0.0f, 100.0f, 100.0f));
        complexElement.put("path", "M10,10 L90,90");
        complexElement.put("fill", "#ff0000");
        complexElement.put("fixedRatio", true);
        complexElement.put("opacity", 0.8f);
        complexElement.put("rotate", 45.0f);
        complexElement.put("flipV", false);
        complexElement.put("lineHeight", 1.5f);
        complexElement.put("content", null);
        complexElement.put("defaultFontName", "Arial");
        complexElement.put("defaultColor", "#000000");
        complexElement.put("start", Arrays.asList(10.0f, 20.0f));
        complexElement.put("end", Arrays.asList(90.0f, 80.0f));
        complexElement.put("points", Arrays.asList("10,10", "50,50", "90,90"));
        complexElement.put("color", "#00ff00");
        complexElement.put("style", "solid");
        complexElement.put("wordSpace", 2.0f);
        // Extra custom fields
        complexElement.put("customProperty", "customValue");
        complexElement.put("isTest", true);
        complexElement.put("testNumber", 42);
        complexElement.put("nestedLevel1", Map.of("level2", Map.of("level3", Map.of("deepKey", "deepValue"))));

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(complexElement));
        slideExtraFields.put("background", request.getSlides().get(0).getExtraFields().get("background"));

        SlideDto slideWithComplexElement = SlideDto.builder().id("complex-slide").extraFields(slideExtraFields).build();

        request = PresentationCreateRequest.builder().slides(List.of(slideWithComplexElement)).build();
        mockResponse = PresentationCreateResponseDto.builder()
                .title("Test Presentation")
                .slides(List.of(slideWithComplexElement))
                .build();

        when(presentationApi.createPresentation(any(PresentationCreateRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slides[0].elements[0].type").value("text"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].id").value("complex-element"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].viewBox[0]").value(0.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].viewBox[1]").value(0.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].viewBox[2]").value(100.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].viewBox[3]").value(100.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].path").value("M10,10 L90,90"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].fill").value("#ff0000"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].fixedRatio").value(true))
                .andExpect(jsonPath("$.data.slides[0].elements[0].opacity").value(0.8))
                .andExpect(jsonPath("$.data.slides[0].elements[0].rotate").value(45.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].flipV").value(false))
                .andExpect(jsonPath("$.data.slides[0].elements[0].lineHeight").value(1.5))
                .andExpect(jsonPath("$.data.slides[0].elements[0].start[0]").value(10.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].start[1]").value(20.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].end[0]").value(90.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].end[1]").value(80.0))
                .andExpect(jsonPath("$.data.slides[0].elements[0].points[0]").value("10,10"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].points[1]").value("50,50"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].points[2]").value("90,90"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].color").value("#00ff00"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].style").value("solid"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].wordSpace").value(2.0))
                // Extra fields
                .andExpect(
                        jsonPath("$.data.slides[0].elements[0].nestedLevel1.level2.level3.deepKey").value("deepValue"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].customProperty").value("customValue"))
                .andExpect(jsonPath("$.data.slides[0].elements[0].isTest").value(true))
                .andExpect(jsonPath("$.data.slides[0].elements[0].testNumber").value(42));
    }

    @Test
    void createPresentation_WithEmptySlides_ShouldReturnValidationError() throws Exception {
        request.setSlides(List.of());

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createPresentation_WithInvalidJson_ShouldReturnError() throws Exception {
        mockMvc.perform(
                post("/api/presentations").contentType(MediaType.APPLICATION_JSON).content("{\"invalid\": \"json\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getPresentationsCollection_WithValidRequest_ShouldReturnPaginatedResponse() throws Exception {
        // Given
        Date createdAt = new Date();
        PresentationListResponseDto presentation = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("Test Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        PaginationDto pagination = new PaginationDto(1, 10, 1L, 1);
        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = new PaginatedResponseDto<>(
                List.of(presentation), pagination);

        when(presentationApi.getAllPresentations(any())).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/presentations").param("page", "1")
                .param("pageSize", "10")
                .param("sort", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("test-id-1"))
                .andExpect(jsonPath("$.data[0].title").value("Test Presentation"))
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(1))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void getPresentationsCollection_WithFilter_ShouldReturnFilteredResults() throws Exception {
        // Given
        Date createdAt = new Date();

        PresentationListResponseDto presentation = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("Filtered Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        PaginationDto pagination = new PaginationDto(1, 10, 1L, 1);
        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = new PaginatedResponseDto<>(
                List.of(presentation), pagination);

        when(presentationApi.getAllPresentations(any())).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/presentations").param("page", "1")
                .param("pageSize", "10")
                .param("sort", "desc")
                .param("filter", "Filtered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Filtered Presentation"));
    }

    @Test
    void getPresentationsCollection_WithDefaultParameters_ShouldReturnResults() throws Exception {
        // Given
        Date createdAt = new Date();
        PresentationListResponseDto presentation = PresentationListResponseDto.builder()
                .id("test-id-1")
                .title("Default Presentation")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        PaginationDto pagination = new PaginationDto(1, 20, 1L, 1);
        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = new PaginatedResponseDto<>(
                List.of(presentation), pagination);

        when(presentationApi.getAllPresentations(any())).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/presentations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getPresentationsCollection_WithEmptyResults_ShouldReturnEmptyPaginatedResponse() throws Exception {
        // Given
        PaginationDto pagination = new PaginationDto(1, 10, 0L, 0);
        PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = new PaginatedResponseDto<>(List.of(),
                pagination);

        when(presentationApi.getAllPresentations(any())).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/presentations").param("page", "1")
                .param("pageSize", "10")
                .param("filter", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0));
    }

    // PUT /presentations/{id}/slides tests
    @Test
    void upsertSlides_WithValidRequest_ShouldReturnNoContent() throws Exception {
        // Given
        String presentationId = "test-presentation-id";

        Map<String, Object> element = new HashMap<>();
        element.put("type", "text");
        element.put("id", "element-1");
        element.put("left", 100.0f);
        element.put("top", 200.0f);
        element.put("width", 300.0f);
        element.put("height", 50.0f);
        element.put("content", "Sample text");

        Map<String, Object> background = new HashMap<>();
        background.put("type", "color");
        background.put("color", "#ffffff");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(element));
        slideExtraFields.put("background", background);

        SlideUpdateRequest slide = SlideUpdateRequest.builder().id("slide-1").extraFields(slideExtraFields).build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(slide)).build();

        doNothing().when(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-idempotency-key")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        verify(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));
    }

    @Test
    void upsertSlides_WithMultipleSlides_ShouldProcessAllSlides() throws Exception {
        // Given
        String presentationId = "test-presentation-id";

        Map<String, Object> element1 = new HashMap<>();
        element1.put("type", "text");
        element1.put("id", "element-1");
        element1.put("content", "First slide text");

        Map<String, Object> element2 = new HashMap<>();
        element2.put("type", "text");
        element2.put("id", "element-2");
        element2.put("content", "Second slide text");

        Map<String, Object> slide1ExtraFields = new HashMap<>();
        slide1ExtraFields.put("elements", List.of(element1));

        Map<String, Object> slide2ExtraFields = new HashMap<>();
        slide2ExtraFields.put("elements", List.of(element2));

        SlideUpdateRequest slide1 = SlideUpdateRequest.builder().id("slide-1").extraFields(slide1ExtraFields).build();

        SlideUpdateRequest slide2 = SlideUpdateRequest.builder().id("slide-2").extraFields(slide2ExtraFields).build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(Arrays.asList(slide1, slide2)).build();

        doNothing().when(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-multiple-slides")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        verify(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));
    }

    @Test
    void upsertSlides_WithComplexSlideElements_ShouldPreserveAllProperties() throws Exception {
        // Given
        String presentationId = "test-presentation-id";

        Map<String, Object> complexElement = new HashMap<>();
        complexElement.put("type", "shape");
        complexElement.put("id", "complex-element");
        complexElement.put("left", 50.0f);
        complexElement.put("top", 75.0f);
        complexElement.put("width", 200.0f);
        complexElement.put("height", 150.0f);
        complexElement.put("viewBox", Arrays.asList(0.0f, 0.0f, 100.0f, 100.0f));
        complexElement.put("path", "M10,10 L90,90");
        complexElement.put("fill", "#ff0000");
        complexElement.put("fixedRatio", true);
        complexElement.put("opacity", 0.8f);
        complexElement.put("rotate", 45.0f);
        complexElement.put("flipV", false);
        complexElement.put("lineHeight", 1.5f);
        complexElement.put("defaultFontName", "Arial");
        complexElement.put("defaultColor", "#000000");
        complexElement.put("start", Arrays.asList(10.0f, 20.0f));
        complexElement.put("end", Arrays.asList(90.0f, 80.0f));
        complexElement.put("points", Arrays.asList("10,10", "50,50", "90,90"));
        complexElement.put("color", "#00ff00");
        complexElement.put("style", "solid");
        complexElement.put("wordSpace", 2.0f);

        Map<String, Object> background = new HashMap<>();
        background.put("type", "gradient");
        background.put("color", "#ffffff");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(complexElement));
        slideExtraFields.put("background", background);

        SlideUpdateRequest slide = SlideUpdateRequest.builder()
                .id("complex-slide")
                .extraFields(slideExtraFields)
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(slide)).build();

        doNothing().when(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-complex-elements")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        verify(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));
    }

    @Test
    void upsertSlides_WithEmptySlidesList_ShouldStillProcess() throws Exception {
        // Given
        String presentationId = "test-presentation-id";
        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of()).build();

        doNothing().when(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-empty-slides")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        verify(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));
    }

    @Test
    void upsertSlides_WithInvalidSlideId_ShouldReturnBadRequest() throws Exception {
        // Given
        String presentationId = "test-presentation-id";
        SlideUpdateRequest slideWithoutId = SlideUpdateRequest.builder()
                .id("") // Invalid: blank ID
                .extraFields(new HashMap<>())
                .build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(slideWithoutId)).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-invalid-slide-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void upsertSlides_WithInvalidElementType_ShouldReturnBadRequest() throws Exception {
        // Given
        String presentationId = "test-presentation-id";

        Map<String, Object> elementWithoutType = new HashMap<>();
        elementWithoutType.put("type", null); // Null type is now allowed with extraFields structure
        elementWithoutType.put("id", "element-1");
        elementWithoutType.put("content", "Sample text");

        Map<String, Object> slideExtraFields = new HashMap<>();
        slideExtraFields.put("elements", List.of(elementWithoutType));

        SlideUpdateRequest slide = SlideUpdateRequest.builder().id("slide-1").extraFields(slideExtraFields).build();

        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(List.of(slide)).build();

        doNothing().when(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));

        // When & Then
        // After refactoring to use extraFields, null types are allowed (no field-level validation)
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-invalid-element-type")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

        verify(slidesApi).upsertSlides(eq(presentationId), any(SlidesUpsertRequest.class));
    }

    @Test
    void upsertSlides_WithNullSlides_ShouldReturnBadRequest() throws Exception {
        // Given
        String presentationId = "test-presentation-id";
        SlidesUpsertRequest request = SlidesUpsertRequest.builder().slides(null).build();

        // When & Then
        mockMvc.perform(put("/api/presentations/{id}/slides", presentationId).contentType(MediaType.APPLICATION_JSON)
                .header("idempotency-key", "test-key-null-slides")
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
    }
}
