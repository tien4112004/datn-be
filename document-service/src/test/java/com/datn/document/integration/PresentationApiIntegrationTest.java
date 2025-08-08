package com.datn.document.integration;

import com.datn.document.dto.SlideDto;
import com.datn.document.dto.SlideDto.SlideElementDto;
import com.datn.document.dto.SlideDto.SlideBackgroundDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.entity.Presentation;
import com.datn.document.enums.SlideElementType;
import com.datn.document.repository.PresentationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class PresentationApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PresentationRepository presentationRepository;
    private MockMvc mockMvc;
    private PresentationCreateRequest request;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        presentationRepository.deleteAll();

        SlideBackgroundDto background = SlideBackgroundDto.builder().type("color").color("#ffffff").build();

        SlideElementDto textElement = SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("text-element-1")
                .left(100.0f)
                .top(200.0f)
                .width(300.0f)
                .height(50.0f)
                .lineHeight(1.2f)
                .content("Welcome to our presentation")
                .defaultFontName("Arial")
                .defaultColor("#000000")
                .wordSpace(1.0f)
                .build();

        SlideDto slide = SlideDto.builder().id("slide-1").elements(List.of(textElement)).background(background).build();

        request = PresentationCreateRequest.builder().slides(List.of(slide)).build();
    }

    @Test
    void createPresentation_EndToEnd_ShouldProcessCompleteRequest() throws Exception {
        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value("Untitled Presentation"))
                .andExpect(jsonPath("$.data.presentation").isArray())
                .andExpect(jsonPath("$.data.presentation.length()").value(1))
                .andExpect(jsonPath("$.data.presentation[0].id").value("slide-1"))
                .andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("text"))
                .andExpect(jsonPath("$.data.presentation[0].elements[0].content").value("Welcome to our presentation"))
                .andExpect(jsonPath("$.data.presentation[0].background.type").value("color"))
                .andExpect(jsonPath("$.data.presentation[0].background.color").value("#ffffff"));
    }

    @Test
    void createPresentation_WithMultipleSlideTypes_ShouldHandleAllTypes() throws Exception {
        SlideElementDto shapeElement = SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("shape-element-1")
                .left(50.0f)
                .top(100.0f)
                .width(200.0f)
                .height(150.0f)
                .viewBox(Arrays.asList(0.0f, 0.0f, 200.0f, 150.0f))
                .path("M50,50 L150,50 L150,100 L50,100 Z")
                .fill("#ff6b6b")
                .fixedRatio(true)
                .opacity(0.9f)
                .rotate(0.0f)
                .flipV(false)
                .build();

        SlideElementDto imageElement = SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("image-element-1")
                .left(300.0f)
                .top(200.0f)
                .width(400.0f)
                .height(300.0f)
                .fixedRatio(true)
                .opacity(1.0f)
                .build();

        SlideBackgroundDto gradientBackground = SlideBackgroundDto.builder()
                .type("gradient")
                .color("linear-gradient(45deg, #ff6b6b, #4ecdc4)")
                .build();

        SlideDto complexSlide = SlideDto.builder()
                .id("complex-slide-1")
                .elements(Arrays.asList(request.getSlides().get(0).getElements().get(0), shapeElement, imageElement))
                .background(gradientBackground)
                .build();

        request = PresentationCreateRequest.builder().slides(List.of(complexSlide)).build();

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.presentation[0].elements.length()").value(3))
                .andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("text"))
                .andExpect(jsonPath("$.data.presentation[0].elements[1].type").value("text"))
                .andExpect(
                        jsonPath("$.data.presentation[0].elements[1].path").value("M50,50 L150,50 L150,100 L50,100 Z"))
                .andExpect(jsonPath("$.data.presentation[0].elements[1].fill").value("#ff6b6b"))
                .andExpect(jsonPath("$.data.presentation[0].elements[1].fixedRatio").value(true))
                .andExpect(jsonPath("$.data.presentation[0].elements[2].type").value("text"))
                .andExpect(jsonPath("$.data.presentation[0].background.type").value("gradient"))
                .andExpect(jsonPath("$.data.presentation[0].background.color")
                        .value("linear-gradient(45deg, #ff6b6b, #4ecdc4)"));
    }

    @Test
    void createPresentation_WithLargePresentationData_ShouldHandleSuccessfully() throws Exception {
        SlideDto[] slides = new SlideDto[5];

        for (int i = 0; i < 5; i++) {
            SlideElementDto element = SlideElementDto.builder()
                    .type(SlideElementType.TEXT)
                    .id("element-" + (i + 1))
                    .left(100.0f)
                    .top(200.0f + (i * 100.0f))
                    .width(300.0f)
                    .height(50.0f)
                    .content("Slide " + (i + 1) + " content")
                    .defaultFontName("Arial")
                    .defaultColor("#000000")
                    .build();

            SlideBackgroundDto background = SlideBackgroundDto.builder()
                    .type("color")
                    .color("#f" + i + "f" + i + "f" + i)
                    .build();

            slides[i] = SlideDto.builder()
                    .id("slide-" + (i + 1))
                    .elements(List.of(element))
                    .background(background)
                    .build();
        }

        request.setSlides(Arrays.asList(slides));

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.presentation.length()").value(5))
                .andExpect(jsonPath("$.data.presentation[0].id").value("slide-1"))
                .andExpect(jsonPath("$.data.presentation[4].id").value("slide-5"))
                .andExpect(jsonPath("$.data.presentation[2].elements[0].content").value("Slide 3 content"));
    }

    @Test
    void createPresentation_WithNullValues_ShouldHandleGracefully() throws Exception {
        SlideElementDto elementWithNulls = SlideElementDto.builder()
                .type(SlideElementType.TEXT)
                .id("element-with-nulls")
                .content("Content with null properties")
                .build();

        SlideDto slideWithNulls = SlideDto.builder().id("slide-with-nulls").elements(List.of(elementWithNulls)).build();

        request.setSlides(List.of(slideWithNulls));

        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.presentation[0].id").value("slide-with-nulls"))
                .andExpect(
                        jsonPath("$.data.presentation[0].elements[0].content").value("Content with null properties"));
    }

    @Test
    void getAllPresentations_WithExistingPresentations_ShouldReturnAllPresentations() throws Exception {
        // Given - Create test data
        LocalDateTime now = LocalDateTime.now();
        Presentation presentation1 = Presentation.builder()
                .title("Integration Test Presentation 1")
                .slides(Collections.emptyList())
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(2))
                .build();

        Presentation presentation2 = Presentation.builder()
                .title("Integration Test Presentation 2")
                .slides(Collections.emptyList())
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build();

        presentationRepository.saveAll(List.of(presentation1, presentation2));

        // When & Then
        mockMvc.perform(get("/api/presentations/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].title",
                        containsInAnyOrder("Integration Test Presentation 1", "Integration Test Presentation 2")))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists());
    }

    @Test
    void getAllPresentations_WithEmptyDatabase_ShouldReturnEmptyArray() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/presentations/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getPresentationsCollection_WithDefaultParameters_ShouldReturnPaginatedResults() throws Exception {
        // Given - Create multiple presentations
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            Presentation presentation = Presentation.builder()
                    .title("Test Presentation " + i)
                    .slides(Collections.emptyList())
                    .createdAt(now.minusHours(5 - i))
                    .updatedAt(now.minusHours(5 - i))
                    .build();
            presentationRepository.save(presentation);
        }

        // When & Then
        mockMvc.perform(get("/api/presentations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(5))
                .andExpect(jsonPath("$.pagination.totalPages").value(1))
                .andExpect(jsonPath("$.pagination.hasNextPage").value(false))
                .andExpect(jsonPath("$.pagination.hasPreviousPage").value(false));
    }

    @Test
    void getPresentationsCollection_WithPaginationParameters_ShouldReturnCorrectPage() throws Exception {
        // Given - Create test data
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 15; i++) {
            Presentation presentation = Presentation.builder()
                    .title("Paginated Presentation " + i)
                    .slides(Collections.emptyList())
                    .createdAt(now.minusHours(15 - i))
                    .updatedAt(now.minusHours(15 - i))
                    .build();
            presentationRepository.save(presentation);
        }

        // When & Then - Test first page
        mockMvc.perform(get("/api/presentations").param("page", "1")
                .param("pageSize", "5")
                .param("sort", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.pagination.pageSize").value(5))
                .andExpect(jsonPath("$.pagination.totalItems").value(15))
                .andExpect(jsonPath("$.pagination.totalPages").value(3))
                .andExpect(jsonPath("$.pagination.hasNextPage").value(true))
                .andExpect(jsonPath("$.pagination.hasPreviousPage").value(false))
                .andExpect(jsonPath("$.data[0].title").value("Paginated Presentation 15"));

        // Test second page
        mockMvc.perform(get("/api/presentations").param("page", "2")
                .param("pageSize", "5")
                .param("sort", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.pagination.currentPage").value(2))
                .andExpect(jsonPath("$.pagination.hasNextPage").value(true))
                .andExpect(jsonPath("$.pagination.hasPreviousPage").value(true));
    }

    @Test
    void getPresentationsCollection_WithFilterParameter_ShouldReturnFilteredResults() throws Exception {
        // Given - Create test data with different titles
        LocalDateTime now = LocalDateTime.now();
        Presentation matchingPresentation1 = Presentation.builder()
                .title("Important Business Presentation")
                .slides(Collections.emptyList())
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(2))
                .build();

        Presentation matchingPresentation2 = Presentation.builder()
                .title("Business Report Analysis")
                .slides(Collections.emptyList())
                .createdAt(now.minusHours(1))
                .updatedAt(now.minusHours(1))
                .build();

        Presentation nonMatchingPresentation = Presentation.builder()
                .title("Technical Documentation")
                .slides(Collections.emptyList())
                .createdAt(now)
                .updatedAt(now)
                .build();

        presentationRepository.saveAll(List.of(matchingPresentation1, matchingPresentation2, nonMatchingPresentation));

        // When & Then
        mockMvc.perform(get("/api/presentations").param("filter", "business")
                .param("sort", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].title",
                        containsInAnyOrder("Important Business Presentation", "Business Report Analysis")))
                .andExpect(jsonPath("$.pagination.totalItems").value(2));
    }

    @Test
    void getPresentationsCollection_WithCaseInsensitiveFilter_ShouldReturnResults() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Presentation presentation = Presentation.builder()
                .title("TEST Presentation for filtering")
                .slides(Collections.emptyList())
                .createdAt(now)
                .updatedAt(now)
                .build();

        presentationRepository.save(presentation);

        // When & Then - Test different case variations
        mockMvc.perform(get("/api/presentations").param("filter", "test").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("TEST Presentation for filtering"));

        mockMvc.perform(get("/api/presentations").param("filter", "TEST").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/presentations").param("filter", "Test").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void getPresentationsCollection_WithSortingParameter_ShouldReturnSortedResults() throws Exception {
        // Given - Create presentations with different creation times
        LocalDateTime now = LocalDateTime.now();
        Presentation oldestPresentation = Presentation.builder()
                .title("Oldest Presentation")
                .slides(Collections.emptyList())
                .createdAt(now.minusDays(3))
                .updatedAt(now.minusDays(3))
                .build();

        Presentation middlePresentation = Presentation.builder()
                .title("Middle Presentation")
                .slides(Collections.emptyList())
                .createdAt(now.minusDays(2))
                .updatedAt(now.minusDays(2))
                .build();

        Presentation newestPresentation = Presentation.builder()
                .title("Newest Presentation")
                .slides(Collections.emptyList())
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusDays(1))
                .build();

        presentationRepository.saveAll(List.of(oldestPresentation, middlePresentation, newestPresentation));

        // Test ascending sort
        mockMvc.perform(get("/api/presentations").param("sort", "asc").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].title").value("Oldest Presentation"))
                .andExpect(jsonPath("$.data[2].title").value("Newest Presentation"));

        // Test descending sort
        mockMvc.perform(get("/api/presentations").param("sort", "desc").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Newest Presentation"))
                .andExpect(jsonPath("$.data[2].title").value("Oldest Presentation"));
    }

    @Test
    void getPresentationsCollection_WithNoMatchingFilter_ShouldReturnEmptyResults() throws Exception {
        // Given - Create presentations that won't match the filter
        LocalDateTime now = LocalDateTime.now();
        Presentation presentation = Presentation.builder()
                .title("Sample Presentation")
                .slides(Collections.emptyList())
                .createdAt(now)
                .updatedAt(now)
                .build();

        presentationRepository.save(presentation);

        // When & Then
        mockMvc.perform(
                get("/api/presentations").param("filter", "nonexistentfilter").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pagination.totalItems").value(0))
                .andExpect(jsonPath("$.pagination.totalPages").value(0));
    }

    @Test
    void getPresentationsCollection_EndToEndWithRealData_ShouldWorkCorrectly() throws Exception {
        // Given - Create a presentation using the POST endpoint first
        mockMvc.perform(post("/api/presentations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // Create additional presentations directly in DB
        LocalDateTime now = LocalDateTime.now();
        Presentation directPresentation = Presentation.builder()
                .title("Direct DB Presentation")
                .slides(Collections.emptyList())
                .createdAt(now)
                .updatedAt(now)
                .build();

        presentationRepository.save(directPresentation);

        // When & Then - Verify both presentations are returned
        mockMvc.perform(get("/api/presentations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].title", hasItems("Untitled Presentation", "Direct DB Presentation")));

        // Test collection endpoint with filter
        mockMvc.perform(get("/api/presentations").param("filter", "Direct").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Direct DB Presentation"));
    }

}