package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.ImageGenerationApi;
import com.datn.datnbe.ai.api.ModelSelectionApi;
import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import com.datn.datnbe.ai.dto.response.ImageGeneratedResponseDto;
import com.datn.datnbe.ai.utils.MappingParamsUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageGenerationManagement implements ImageGenerationApi {
    @Value("${ai.api.image-endpoint}")
    @NonFinal
    String IMAGE_API_ENDPOINT = "/api/image/generation";

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
        log.info("Image generation end: {}", generatedImage);

        if (generatedImage.getError() != null) {
            log.error("Error during image generation: {}", generatedImage.getError());
            throw new AppException(ErrorCode.GENERATION_ERROR, generatedImage.getError());
        }

        return generatedImage.getImages().stream().map(this::uploadImageToStorage).toList();
    }

    private MultipartFile uploadImageToStorage(String base64Image) {
        // Decode the base64 string to bytes
        byte[] decoded = Base64.getDecoder().decode(base64Image);

        File file = new File("AI_generated_image.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        } catch (Exception e) {
            log.error("Error writing image file", e);
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }

        return (MultipartFile) file;
    }
}
