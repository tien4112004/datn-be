package com.datn.document.integration;

import com.datn.document.dto.SlideBackgroundDto;
import com.datn.document.dto.SlideDto;
import com.datn.document.dto.SlideElementDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.enums.SlideElementType;
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

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class PresentationApiIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;
        private PresentationCreateRequest request;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

                SlideBackgroundDto background = SlideBackgroundDto.builder()
                                .type("color")
                                .color("#ffffff")
                                .build();

                SlideElementDto textElement = SlideElementDto.builder()
                                .type(SlideElementType.CONTENT)
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

                SlideDto slide = SlideDto.builder()
                                .id("slide-1")
                                .elements(List.of(textElement))
                                .background(background)
                                .build();

                request = PresentationCreateRequest.builder()
                                .slides(List.of(slide))
                                .build();
        }

        @Test
        void createPresentation_EndToEnd_ShouldProcessCompleteRequest() throws Exception {
                mockMvc.perform(post("/api/presentations")
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("content"))
                                .andExpect(jsonPath("$.data.presentation[0].elements[0].content")
                                                .value("Welcome to our presentation"))
                                .andExpect(jsonPath("$.data.presentation[0].background.type").value("color"))
                                .andExpect(jsonPath("$.data.presentation[0].background.color").value("#ffffff"));
        }

        @Test
        void createPresentation_WithMultipleSlideTypes_ShouldHandleAllTypes() throws Exception {
                SlideElementDto shapeElement = SlideElementDto.builder()
                                .type(SlideElementType.CONTENT)
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
                                .type(SlideElementType.CONTENT)
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
                                .elements(Arrays.asList(request.getSlides().get(0).getElements().get(0), shapeElement,
                                                imageElement))
                                .background(gradientBackground)
                                .build();

                request = PresentationCreateRequest.builder()
                                .slides(List.of(complexSlide))
                                .build();

                mockMvc.perform(post("/api/presentations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.presentation[0].elements.length()").value(3))
                                .andExpect(jsonPath("$.data.presentation[0].elements[0].type").value("content"))
                                .andExpect(jsonPath("$.data.presentation[0].elements[1].type").value("content"))
                                .andExpect(
                                                jsonPath("$.data.presentation[0].elements[1].path")
                                                                .value("M50,50 L150,50 L150,100 L50,100 Z"))
                                .andExpect(jsonPath("$.data.presentation[0].elements[1].fill").value("#ff6b6b"))
                                .andExpect(jsonPath("$.data.presentation[0].elements[1].fixedRatio").value(true))
                                .andExpect(jsonPath("$.data.presentation[0].elements[2].type").value("content"))
                                .andExpect(jsonPath("$.data.presentation[0].background.type").value("gradient"))
                                .andExpect(jsonPath("$.data.presentation[0].background.color")
                                                .value("linear-gradient(45deg, #ff6b6b, #4ecdc4)"));
        }

        @Test
        void createPresentation_WithLargePresentationData_ShouldHandleSuccessfully() throws Exception {
                SlideDto[] slides = new SlideDto[5];

                for (int i = 0; i < 5; i++) {
                        SlideElementDto element = SlideElementDto.builder()
                                        .type(SlideElementType.CONTENT)
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

                mockMvc.perform(post("/api/presentations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.presentation.length()").value(5))
                                .andExpect(jsonPath("$.data.presentation[0].id").value("slide-1"))
                                .andExpect(jsonPath("$.data.presentation[4].id").value("slide-5"))
                                .andExpect(jsonPath("$.data.presentation[2].elements[0].content")
                                                .value("Slide 3 content"));
        }

        @Test
        void createPresentation_WithNullValues_ShouldHandleGracefully() throws Exception {
                SlideElementDto elementWithNulls = SlideElementDto.builder()
                                .type(SlideElementType.CONTENT)
                                .id("element-with-nulls")
                                .content("Content with null properties")
                                .build();

                SlideDto slideWithNulls = SlideDto.builder()
                                .id("slide-with-nulls")
                                .elements(List.of(elementWithNulls))
                                .build();

                request.setSlides(List.of(slideWithNulls));

                mockMvc.perform(post("/api/presentations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.presentation[0].id").value("slide-with-nulls"))
                                .andExpect(
                                                jsonPath("$.data.presentation[0].elements[0].content")
                                                                .value("Content with null properties"));
        }
}