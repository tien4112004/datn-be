package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.request.ImagePromptRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageGenerationApi {
    List<MultipartFile> generateImage(ImagePromptRequest request);
    List<MultipartFile> generateMockImage(ImagePromptRequest request);
}
