package com.datn.datnbe.ai.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.dto.request.UpdateModelStatusRequest;
import com.datn.datnbe.ai.dto.response.ModelResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ModelConfigurationController.class)
class ModelConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelSelectionApi modelSelectionApi;

    @Autowired
    private ObjectMapper objectMapper;

    private List<ModelResponseDto> mockModels;
    private ModelResponseDto mockModel;

    @BeforeEach
    void setUp() {
        mockModel = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .provider("openai")
                .isEnabled(true)
                .isDefault(false)
                .build();

        ModelResponseDto mockModel2 = ModelResponseDto.builder()
                .modelId("2")
                .modelName("claude-3")
                .displayName("Claude 3")
                .provider("anthropic")
                .isEnabled(false)
                .isDefault(true)
                .build();

        ModelResponseDto mockModel3 = ModelResponseDto.builder()
                .modelId("3")
                .modelName("gemini-pro")
                .displayName("Gemini Pro")
                .provider("google")
                .isEnabled(true)
                .isDefault(false)
                .build();

        mockModels = Arrays.asList(mockModel, mockModel2, mockModel3);
    }

    // ===============================
    // Tests for GET /api/models
    // ===============================

    @Test
        @DisplayName("Should return list of all models successfully")
        void getAllModels_WithValidData_ShouldReturnSuccessResponse() throws Exception {
                // Given
                when(modelSelectionApi.getModelConfigurations()).thenReturn(mockModels);

                // When & Then
                mockMvc
                                .perform(get("/api/models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(3))
                                .andExpect(jsonPath("$.data[0].modelId").value("1"))
                                .andExpect(jsonPath("$.data[0].modelName").value("gpt-4"))
                                .andExpect(jsonPath("$.data[0].displayName").value("GPT-4"))
                                .andExpect(jsonPath("$.data[0].provider").value("openai"))
                                .andExpect(jsonPath("$.data[0].enabled").value(true))
                                .andExpect(jsonPath("$.data[0].default").value(false))
                                .andExpect(jsonPath("$.data[1].modelId").value("2"))
                                .andExpect(jsonPath("$.data[1].modelName").value("claude-3"))
                                .andExpect(jsonPath("$.data[1].displayName").value("Claude 3"))
                                .andExpect(jsonPath("$.data[1].provider").value("anthropic"))
                                .andExpect(jsonPath("$.data[1].enabled").value(false))
                                .andExpect(jsonPath("$.data[1].default").value(true))
                                .andExpect(jsonPath("$.data[2].modelId").value("3"))
                                .andExpect(jsonPath("$.data[2].modelName").value("gemini-pro"))
                                .andExpect(jsonPath("$.data[2].displayName").value("Gemini Pro"))
                                .andExpect(jsonPath("$.data[2].provider").value("google"))
                                .andExpect(jsonPath("$.data[2].enabled").value(true))
                                .andExpect(jsonPath("$.data[2].default").value(false));
        }

    @Test
        @DisplayName("Should return empty list when no models exist")
        void getAllModels_WithEmptyData_ShouldReturnEmptyListResponse() throws Exception {
                // Given
                when(modelSelectionApi.getModelConfigurations()).thenReturn(Collections.emptyList());

                // When & Then
                mockMvc
                                .perform(get("/api/models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));
        }

    @Test
        @DisplayName("Should handle service exception when getting models")
        void getAllModels_WithServiceException_ShouldReturnErrorResponse() throws Exception {
                // Given
                when(modelSelectionApi.getModelConfigurations())
                                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                                                "Database connection failed"));

                // When & Then
                mockMvc
                                .perform(get("/api/models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(500))
                                .andExpect(jsonPath("$.message").value("Database connection failed"))
                                .andExpect(jsonPath("$.errorCode").value("UNCATEGORIZED_ERROR"));
        }

    // ===============================
    // Tests for GET /api/models/text-models
    // ===============================

    @Test
    @DisplayName("Should return list of text models successfully")
    void getTextModels_WithValidData_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ModelResponseDto> textModels = Arrays.asList(
                ModelResponseDto.builder()
                        .modelId("1")
                        .modelName("gpt-4")
                        .displayName("GPT-4")
                        .provider("openai")
                        .isEnabled(true)
                        .isDefault(true)
                        .build(),
                ModelResponseDto.builder()
                        .modelId("2")
                        .modelName("claude-3")
                        .displayName("Claude 3")
                        .provider("anthropic")
                        .isEnabled(true)
                        .isDefault(false)
                        .build());

        when(modelSelectionApi.getTextModelModelConfigurations()).thenReturn(textModels);

        // When & Then
        mockMvc.perform(get("/api/models/text-models").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].modelId").value("1"))
                .andExpect(jsonPath("$.data[0].modelName").value("gpt-4"))
                .andExpect(jsonPath("$.data[0].displayName").value("GPT-4"))
                .andExpect(jsonPath("$.data[0].provider").value("openai"))
                .andExpect(jsonPath("$.data[0].enabled").value(true))
                .andExpect(jsonPath("$.data[0].default").value(true))
                .andExpect(jsonPath("$.data[1].modelId").value("2"))
                .andExpect(jsonPath("$.data[1].modelName").value("claude-3"))
                .andExpect(jsonPath("$.data[1].displayName").value("Claude 3"))
                .andExpect(jsonPath("$.data[1].provider").value("anthropic"))
                .andExpect(jsonPath("$.data[1].enabled").value(true))
                .andExpect(jsonPath("$.data[1].default").value(false));
    }

    @Test
        @DisplayName("Should return empty list when no text models exist")
        void getTextModels_WithEmptyData_ShouldReturnEmptyListResponse() throws Exception {
                // Given
                when(modelSelectionApi.getTextModelModelConfigurations()).thenReturn(Collections.emptyList());

                // When & Then
                mockMvc
                                .perform(get("/api/models/text-models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));
        }

    @Test
        @DisplayName("Should handle service exception when getting text models")
        void getTextModels_WithServiceException_ShouldReturnErrorResponse() throws Exception {
                // Given
                when(modelSelectionApi.getTextModelModelConfigurations())
                                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                                                "Database connection failed"));

                // When & Then
                mockMvc
                                .perform(get("/api/models/text-models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(500))
                                .andExpect(jsonPath("$.message").value("Database connection failed"))
                                .andExpect(jsonPath("$.errorCode").value("UNCATEGORIZED_ERROR"));
        }

    // ===============================
    // Tests for GET /api/models/image-models
    // ===============================

    @Test
    @DisplayName("Should return list of image models successfully")
    void getImageModels_WithValidData_ShouldReturnSuccessResponse() throws Exception {
        // Given
        List<ModelResponseDto> imageModels = Arrays.asList(
                ModelResponseDto.builder()
                        .modelId("10")
                        .modelName("dall-e-3")
                        .displayName("DALL-E 3")
                        .provider("openai")
                        .isEnabled(true)
                        .isDefault(true)
                        .build(),
                ModelResponseDto.builder()
                        .modelId("11")
                        .modelName("midjourney")
                        .displayName("Midjourney")
                        .provider("midjourney")
                        .isEnabled(false)
                        .isDefault(false)
                        .build());

        when(modelSelectionApi.getImageModelConfigurations()).thenReturn(imageModels);

        // When & Then
        mockMvc.perform(get("/api/models/image-models").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].modelId").value("10"))
                .andExpect(jsonPath("$.data[0].modelName").value("dall-e-3"))
                .andExpect(jsonPath("$.data[0].displayName").value("DALL-E 3"))
                .andExpect(jsonPath("$.data[0].provider").value("openai"))
                .andExpect(jsonPath("$.data[0].enabled").value(true))
                .andExpect(jsonPath("$.data[0].default").value(true))
                .andExpect(jsonPath("$.data[1].modelId").value("11"))
                .andExpect(jsonPath("$.data[1].modelName").value("midjourney"))
                .andExpect(jsonPath("$.data[1].displayName").value("Midjourney"))
                .andExpect(jsonPath("$.data[1].provider").value("midjourney"))
                .andExpect(jsonPath("$.data[1].enabled").value(false))
                .andExpect(jsonPath("$.data[1].default").value(false));
    }

    @Test
        @DisplayName("Should return empty list when no image models exist")
        void getImageModels_WithEmptyData_ShouldReturnEmptyListResponse() throws Exception {
                // Given
                when(modelSelectionApi.getImageModelConfigurations()).thenReturn(Collections.emptyList());

                // When & Then
                mockMvc
                                .perform(get("/api/models/image-models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));
        }

    @Test
        @DisplayName("Should handle service exception when getting image models")
        void getImageModels_WithServiceException_ShouldReturnErrorResponse() throws Exception {
                // Given
                when(modelSelectionApi.getImageModelConfigurations())
                                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_ERROR,
                                                "Database connection failed"));

                // When & Then
                mockMvc
                                .perform(get("/api/models/image-models").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(500))
                                .andExpect(jsonPath("$.message").value("Database connection failed"))
                                .andExpect(jsonPath("$.errorCode").value("UNCATEGORIZED_ERROR"));
        }

    // ===============================
    // Tests for PATCH /api/models/{id}
    // ===============================

    @Test
    @DisplayName("Should update model status successfully with both enabled and default")
    void updateModelStatus_WithBothEnabledAndDefault_ShouldReturnSuccessResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(true, true);
        ModelResponseDto updatedModel = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .provider("openai")
                .isEnabled(true)
                .isDefault(true)
                .build();

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenReturn(updatedModel);

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelId").value("1"))
                .andExpect(jsonPath("$.data.modelName").value("gpt-4"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.default").value(true));
    }

    @Test
    @DisplayName("Should update model status successfully with only enabled")
    void updateModelStatus_WithOnlyEnabled_ShouldReturnSuccessResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(false, null);
        ModelResponseDto updatedModel = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .provider("openai")
                .isEnabled(false)
                .isDefault(false)
                .build();

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenReturn(updatedModel);

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelId").value("1"))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.default").value(false));
    }

    @Test
    @DisplayName("Should update model status successfully with only default")
    void updateModelStatus_WithOnlyDefault_ShouldReturnSuccessResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(null, true);
        ModelResponseDto updatedModel = ModelResponseDto.builder()
                .modelId("1")
                .modelName("gpt-4")
                .displayName("GPT-4")
                .provider("openai")
                .isEnabled(true)
                .isDefault(true)
                .build();

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenReturn(updatedModel);

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelId").value("1"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.default").value(true));
    }

    @Test
    @DisplayName("Should handle model not found exception")
    void updateModelStatus_WithNonExistentModel_ShouldReturnNotFoundResponse() throws Exception {
        // Given
        Integer modelId = 999;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(true, false);

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Model not found"))
                .andExpect(jsonPath("$.errorCode").value("MODEL_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should handle invalid model status exception")
    void updateModelStatus_WithInvalidStatus_ShouldReturnBadRequestResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(false, true); // Disabled but default

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class))).thenThrow(
                new AppException(ErrorCode.INVALID_MODEL_STATUS, "A model cannot be default if it is disabled"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("A model cannot be default if it is disabled"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MODEL_STATUS"));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void updateModelStatus_WithEmptyRequestBody_ShouldReturnBadRequestResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(null, null);

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVALID_MODEL_STATUS,
                        "At least one of isEnabled or isDefault must be provided"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("At least one of isEnabled or isDefault must be provided"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MODEL_STATUS"));
    }

    @Test
    @DisplayName("Should handle malformed JSON request body")
    void updateModelStatus_WithMalformedJson_ShouldReturnBadRequestResponse() throws Exception {
        // Given
        Integer modelId = 1;
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(
                patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON).content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should handle missing request body")
    void updateModelStatus_WithMissingRequestBody_ShouldReturnBadRequestResponse() throws Exception {
        // Given
        Integer modelId = 1;

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service layer exception")
    void updateModelStatus_WithServiceException_ShouldReturnInternalServerErrorResponse() throws Exception {
        // Given
        Integer modelId = 1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(true, false);

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Internal server error"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.errorCode").value("UNCATEGORIZED_ERROR"));
    }

    // ===============================
    // Additional edge case tests
    // ===============================

    @Test
    @DisplayName("Should handle negative model ID in path")
    void updateModelStatus_WithNegativeModelId_ShouldCallServiceWithNegativeId() throws Exception {
        // Given
        Integer modelId = -1;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(true, false);

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MODEL_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should handle zero model ID in path")
    void updateModelStatus_WithZeroModelId_ShouldCallServiceWithZeroId() throws Exception {
        // Given
        Integer modelId = 0;
        UpdateModelStatusRequest request = new UpdateModelStatusRequest(true, false);

        when(modelSelectionApi.setModelStatus(eq(modelId), any(UpdateModelStatusRequest.class)))
                .thenThrow(new AppException(ErrorCode.MODEL_NOT_FOUND, "Model not found"));

        // When & Then
        mockMvc.perform(patch("/api/models/{id}", modelId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MODEL_NOT_FOUND"));
    }
}
