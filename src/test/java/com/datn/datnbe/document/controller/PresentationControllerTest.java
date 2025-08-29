package com.datn.datnbe.document.controller;

import com.datn.datnbe.document.api.PresentationApi;
import com.datn.datnbe.document.dto.SlideDto;
import com.datn.datnbe.document.dto.SlideDto.SlideElementDto;
import com.datn.datnbe.document.dto.request.PresentationCreateRequest;
import com.datn.datnbe.document.dto.response.PresentationCreateResponseDto;
import com.datn.datnbe.document.dto.response.PresentationListResponseDto;
import com.datn.datnbe.document.presentation.PresentationController;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PresentationController.class)
class PresentationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PresentationApi presentationApi;

	@Autowired
	private ObjectMapper objectMapper;

	private PresentationCreateRequest request;
	private PresentationCreateResponseDto mockResponse;

	@BeforeEach
	void setUp() {
		SlideDto.SlideBackgroundDto background = SlideDto.SlideBackgroundDto.builder().type("color")
				.color("#ffffff").build();

		SlideElementDto element = SlideElementDto.builder()
				.type("text")
				.id("element-1")
				.left(100.0f)
				.top(200.0f)
				.width(300.0f)
				.height(50.0f)
				.content("Sample text")
				.defaultFontName("Arial")
				.defaultColor("#000000")
				.build();

		SlideDto slide = SlideDto.builder().id("slide-1").elements(List.of(element)).background(background)
				.build();

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
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data.title").value("Test Presentation"))
				.andExpect(jsonPath("$.data.presentation").isArray())
				.andExpect(jsonPath("$.data.presentation[0].id").value("slide-1"))
				.andExpect(jsonPath("$.data.presentation[0].elements").isArray())
				.andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("text"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].id").value("element-1"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].content").value("Sample text"))
				.andExpect(jsonPath("$.data.presentation[0].background.type").value("color"))
				.andExpect(jsonPath("$.data.presentation[0].background.color").value("#ffffff"));
	}

	@Test
	void createPresentation_WithMultipleSlides_ShouldReturnAllSlides() throws Exception {
		SlideDto secondSlide = SlideDto.builder()
				.id("slide-2")
				.elements(request.getSlides().get(0).getElements())
				.background(request.getSlides().get(0).getBackground())
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
				.andExpect(jsonPath("$.data.presentation").isArray())
				.andExpect(jsonPath("$.data.presentation.length()").value(2))
				.andExpect(jsonPath("$.data.presentation[0].id").value("slide-1"))
				.andExpect(jsonPath("$.data.presentation[1].id").value("slide-2"));
	}

	@Test
	void createPresentation_WithComplexElements_ShouldPreserveAllProperties() throws Exception {
		SlideElementDto complexElement = SlideElementDto.builder()
				.type("text")
				.id("complex-element")
				.left(50.0f)
				.top(75.0f)
				.width(200.0f)
				.height(150.0f)
				.viewBox(Arrays.asList(0.0f, 0.0f, 100.0f, 100.0f))
				.path("M10,10 L90,90")
				.fill("#ff0000")
				.fixedRatio(true)
				.opacity(0.8f)
				.rotate(45.0f)
				.flipV(false)
				.lineHeight(1.5f)
				.content(null)
				.defaultFontName("Arial")
				.defaultColor("#000000")
				.start(Arrays.asList(10.0f, 20.0f))
				.end(Arrays.asList(90.0f, 80.0f))
				.points(Arrays.asList("10,10", "50,50", "90,90"))
				.color("#00ff00")
				.style("solid")
				.wordSpace(2.0f)
				.build();

		SlideDto slideWithComplexElement = SlideDto.builder()
				.id("complex-slide")
				.elements(List.of(complexElement))
				.background(request.getSlides().get(0).getBackground())
				.build();

		request = PresentationCreateRequest.builder().slides(List.of(slideWithComplexElement)).build();
		mockResponse = PresentationCreateResponseDto.builder()
				.title("Test Presentation")
				.slides(List.of(slideWithComplexElement))
				.build();

		when(presentationApi.createPresentation(any(PresentationCreateRequest.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("text"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].id").value("complex-element"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].viewBox[0]").value(0.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].viewBox[1]").value(0.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].viewBox[2]").value(100.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].viewBox[3]").value(100.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].path").value("M10,10 L90,90"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].fill").value("#ff0000"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].fixedRatio").value(true))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].opacity").value(0.8))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].rotate").value(45.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].flipV").value(false))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].lineHeight").value(1.5))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].start[0]").value(10.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].start[1]").value(20.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].end[0]").value(90.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].end[1]").value(80.0))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].points[0]").value("10,10"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].points[1]").value("50,50"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].points[2]").value("90,90"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].color").value("#00ff00"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].style").value("solid"))
				.andExpect(jsonPath("$.data.presentation[0].elements[0].wordSpace").value(2.0));
	}

	@Test
	void createPresentation_WithEmptySlides_ShouldReturnValidationError() throws Exception {
		request.setSlides(List.of());

		mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.data.slides").exists());
	}

	@Test
	void createPresentation_WithInvalidJson_ShouldReturnError() throws Exception {
		mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"invalid\": \"json\"}"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void getAllPresentations_ShouldReturnListOfPresentations() throws Exception {
		// Given
		LocalDateTime createdAt = LocalDateTime.now();
		PresentationListResponseDto presentation1 = PresentationListResponseDto.builder()
				.id("test-id-1")
				.title("First Presentation")
				.createdAt(createdAt)
				.updatedAt(createdAt)
				.build();

		PresentationListResponseDto presentation2 = PresentationListResponseDto.builder()
				.id("test-id-2")
				.title("Second Presentation")
				.createdAt(createdAt.plusHours(1))
				.updatedAt(createdAt.plusHours(1))
				.build();

		List<PresentationListResponseDto> presentations = Arrays.asList(presentation1, presentation2);

		when(presentationApi.getAllPresentations()).thenReturn(presentations);

		// When & Then
		mockMvc.perform(get("/api/presentations/all").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].id").value("test-id-1"))
				.andExpect(jsonPath("$.data[0].title").value("First Presentation"))
				.andExpect(jsonPath("$.data[1].id").value("test-id-2"))
				.andExpect(jsonPath("$.data[1].title").value("Second Presentation"));
	}

	@Test
	void getAllPresentations_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
		// Given
		when(presentationApi.getAllPresentations()).thenReturn(List.of());

		// When & Then
		mockMvc.perform(get("/api/presentations/all")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void getPresentationsCollection_WithValidRequest_ShouldReturnPaginatedResponse() throws Exception {
		// Given
		LocalDateTime createdAt = LocalDateTime.now();
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
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
		LocalDateTime createdAt = LocalDateTime.now();

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
		LocalDateTime createdAt = LocalDateTime.now();
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
		PaginatedResponseDto<PresentationListResponseDto> paginatedResponse = new PaginatedResponseDto<>(
				List.of(),
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
}