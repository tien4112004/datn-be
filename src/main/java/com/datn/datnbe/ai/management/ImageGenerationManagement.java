package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
import com.datn.datnbe.sharedkernel.Base64MultipartFile;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageGenerationManagement implements ImageGenerationApi {
    @Value("${ai.api.image-endpoint}")
    @NonFinal
    String IMAGE_API_ENDPOINT;

    AIApiClient aiApiClient;

    ModelSelectionApi modelSelectionApi;

    @Override
    public List<MultipartFile> generateImage(ImagePromptRequest request) {
        log.info("Image generation start");

        if (!modelSelectionApi.isModelEnabled(request.getModel())) {
            log.error("Model {} is not enabled for image generation", request.getModel());
            throw new AppException(ErrorCode.MODEL_NOT_ENABLED);
        }

        //TODO: check if model supports image generation

        ImageGeneratedResponseDto generatedImage = aiApiClient
                .post(IMAGE_API_ENDPOINT, MappingParamsUtils.constructParams(request), ImageGeneratedResponseDto.class);

        if (generatedImage.getError() != null || generatedImage.getImages() == null
                || generatedImage.getImages().isEmpty()) {
            log.error("Error during image generation: {}", generatedImage.getError());
            throw new AppException(ErrorCode.GENERATION_ERROR, generatedImage.getError());
        }

        log.info("Image generation completed");

        return generatedImage.getImages().stream().map(this::convertBase64ToMultipartFile).toList();
    }

    private MultipartFile convertBase64ToMultipartFile(String base64Image) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Image);

            // Create and return MultipartFile
            return new Base64MultipartFile(decoded, "AI_generated_image.png", "image/png", "image");

        } catch (IllegalArgumentException e) {
            log.error("Error decoding base64 image", e);
            throw new AppException(ErrorCode.INVALID_BASE64_FORMAT);
        } catch (Exception e) {
            log.error("Error converting base64 to MultipartFile", e);
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }
}
