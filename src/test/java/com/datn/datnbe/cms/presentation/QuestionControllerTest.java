package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.QuestionApi;
import com.datn.datnbe.cms.dto.request.QuestionCollectionRequest;
import com.datn.datnbe.cms.dto.request.QuestionCreateRequest;
import com.datn.datnbe.cms.dto.request.QuestionUpdateRequest;
import com.datn.datnbe.cms.dto.response.QuestionResponseDto;
import com.datn.datnbe.cms.dto.response.BatchCreateQuestionResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = QuestionController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@DisplayName("QuestionController Tests")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionApi questionApi;

    @MockitoBean
    private SecurityContextUtils securityContextUtils;

    private QuestionResponseDto testResponse;
    private QuestionCreateRequest createRequest;
    private QuestionUpdateRequest updateRequest;
    private PaginatedResponseDto<QuestionResponseDto> paginatedResponse;

    @BeforeEach
    void setUp() {
        testResponse = QuestionResponseDto.builder()
                .id("q-001")
                .type("MULTIPLE_CHOICE")
                .difficulty("KNOWLEDGE")
                .title("Test Question")
                .explanation("Test explanation")
                .points(5)
                .ownerId("user-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = QuestionCreateRequest.builder()
                .type("MULTIPLE_CHOICE")
                .difficulty("KNOWLEDGE")
                .title("New Question")
                .explanation("New explanation")
                .points(10)
                .build();

        updateRequest = QuestionUpdateRequest.builder().title("Updated Title").build();

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(1)
                .pageSize(10)
                .totalItems(1)
                .totalPages(1)
                .build();

        paginatedResponse = PaginatedResponseDto.<QuestionResponseDto>builder()
                .data(Arrays.asList(testResponse))
                .pagination(pagination)
                .build();
    }

    @Test
    @DisplayName("Should return personal questions when bankType=personal")
    void getAllQuestions_WithPersonalBankType_ReturnsPersonalQuestions() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        when(questionApi.getAllQuestions(any(QuestionCollectionRequest.class), eq("user-123")))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/questionbank")
                .param("bankType", "personal")
                .param("page", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is("q-001")))
                .andExpect(jsonPath("$.pagination.currentPage", is(1)));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).getAllQuestions(any(), eq("user-123"));
    }

    @Test
    @DisplayName("Should return public questions when bankType=public")
    void getAllQuestions_WithPublicBankType_ReturnsPublicQuestions() throws Exception {

        when(questionApi.getAllQuestions(any(QuestionCollectionRequest.class), isNull()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/questionbank")
                .param("bankType", "public")
                .param("page", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.pagination.currentPage", is(1)));

        verify(questionApi, times(1)).getAllQuestions(any(), isNull());
    }

    @Test
    @DisplayName("Should return questions with search parameter")
    void getAllQuestions_WithSearch_ReturnsFilteredQuestions() throws Exception {

        when(questionApi.getAllQuestions(any(QuestionCollectionRequest.class), isNull()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/questionbank")
                .param("bankType", "public")
                .param("page", "1")
                .param("pageSize", "10")
                .param("search", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(questionApi, times(1)).getAllQuestions(any(QuestionCollectionRequest.class), isNull());
    }

    @Test
    @DisplayName("Should create a new question successfully")
    void createQuestion_WithValidRequest_CreatesQuestionSuccessfully() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        
        BatchCreateQuestionResponseDto batchResponse = BatchCreateQuestionResponseDto.builder()
                .successful(Arrays.asList(testResponse))
                .failed(Collections.emptyList())
                .totalProcessed(1)
                .totalSuccessful(1)
                .totalFailed(0)
                .build();
        
        when(questionApi.createQuestionsBatchWithPartialSuccess(anyList(), eq("user-123")))
                .thenReturn(batchResponse);

        mockMvc.perform(post("/api/questionbank")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(createRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.successful", hasSize(1)))
                .andExpect(jsonPath("$.data.successful[0].id", is("q-001")))
                .andExpect(jsonPath("$.data.successful[0].ownerId", is("user-123")))
                .andExpect(jsonPath("$.data.failed", hasSize(0)))
                .andExpect(jsonPath("$.data.totalProcessed", is(1)))
                .andExpect(jsonPath("$.data.totalSuccessful", is(1)))
                .andExpect(jsonPath("$.data.totalFailed", is(0)));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).createQuestionsBatchWithPartialSuccess(anyList(), eq("user-123"));
    }

    @Test
    @DisplayName("Should return 400 when title is missing")
    void createQuestion_WithoutTitle_ReturnsBadRequest() throws Exception {

        QuestionCreateRequest invalidRequest = QuestionCreateRequest.builder()
                .type("MULTIPLE_CHOICE")
                .difficulty("KNOWLEDGE")

                .build();

        mockMvc.perform(post("/api/questionbank").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());

        verify(questionApi, never()).createQuestion(any(), any());
    }

    @Test
    @DisplayName("Should retrieve a question by ID")
    void getQuestionById_WithValidId_ReturnsQuestion() throws Exception {

        when(questionApi.getQuestionById("q-001")).thenReturn(testResponse);

        mockMvc.perform(get("/api/questionbank/q-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("q-001")))
                .andExpect(jsonPath("$.data.title", is("Test Question")));

        verify(questionApi, times(1)).getQuestionById("q-001");
    }

    @Test
    @DisplayName("Should return 404 when question not found")
    void getQuestionById_WithInvalidId_ReturnsNotFound() throws Exception {

        when(questionApi.getQuestionById("q-invalid"))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found"));

        mockMvc.perform(get("/api/questionbank/q-invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("RESOURCE_NOT_FOUND")));

        verify(questionApi, times(1)).getQuestionById("q-invalid");
    }

    @Test
    @DisplayName("Should update a question successfully")
    void updateQuestion_WithValidRequest_UpdatesQuestionSuccessfully() throws Exception {

        QuestionResponseDto updatedResponse = QuestionResponseDto.builder()
                .id("q-001")
                .type("MULTIPLE_CHOICE")
                .difficulty("KNOWLEDGE")
                .title("Updated Title")
                .explanation("Test explanation")
                .points(5)
                .ownerId("user-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        when(questionApi.updateQuestion("q-001", updateRequest, "user-123")).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/questionbank/q-001").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("q-001")))
                .andExpect(jsonPath("$.data.title", is("Updated Title")));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).updateQuestion("q-001", updateRequest, "user-123");
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent question")
    void updateQuestion_WithInvalidId_ReturnsNotFound() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        when(questionApi.updateQuestion("q-invalid", updateRequest, "user-123"))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found"));

        mockMvc.perform(put("/api/questionbank/q-invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).updateQuestion(eq("q-invalid"), any(), eq("user-123"));
    }

    @Test
    @DisplayName("Should return 403 when user tries to update question they don't own")
    void updateQuestion_WithoutOwnership_ReturnsForbidden() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-456");
        when(questionApi.updateQuestion("q-001", updateRequest, "user-456"))
                .thenThrow(new AppException(ErrorCode.FORBIDDEN, "You do not have permission to modify this question"));

        mockMvc.perform(put("/api/questionbank/q-001").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("FORBIDDEN")));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).updateQuestion(eq("q-001"), any(), eq("user-456"));
    }

    @Test
    @DisplayName("Should delete a question successfully")
    void deleteQuestion_WithValidId_DeletesQuestionSuccessfully() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        doNothing().when(questionApi).deleteQuestion("q-001", "user-123");

        mockMvc.perform(delete("/api/questionbank/q-001").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).deleteQuestion("q-001", "user-123");
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent question")
    void deleteQuestion_WithInvalidId_ReturnsNotFound() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-123");
        doThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Question not found")).when(questionApi)
                .deleteQuestion("q-invalid", "user-123");

        mockMvc.perform(delete("/api/questionbank/q-invalid").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).deleteQuestion("q-invalid", "user-123");
    }

    @Test
    @DisplayName("Should return 403 when user tries to delete question they don't own")
    void deleteQuestion_WithoutOwnership_ReturnsForbidden() throws Exception {

        when(securityContextUtils.getCurrentUserId()).thenReturn("user-456");
        doThrow(new AppException(ErrorCode.FORBIDDEN, "You do not have permission to modify this question")).when(questionApi)
                .deleteQuestion("q-001", "user-456");

        mockMvc.perform(delete("/api/questionbank/q-001").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("FORBIDDEN")));

        verify(securityContextUtils, times(1)).getCurrentUserId();
        verify(questionApi, times(1)).deleteQuestion("q-001", "user-456");
    }
}
