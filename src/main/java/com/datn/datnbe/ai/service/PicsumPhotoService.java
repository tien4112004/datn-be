package com.datn.datnbe.ai.service;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.utils.Base64MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class PicsumPhotoService {

    private static final String PICSUM_BASE_URL = "https://picsum.photos";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient.Builder webClientBuilder;

    public MultipartFile downloadRandomImage(int width, int height) {
        String url = String.format("%s/%d/%d?random=%d", PICSUM_BASE_URL, width, height, System.currentTimeMillis());

        log.info("Downloading image from picsum.photos: {}", url);

        try {
            byte[] imageBytes = webClientBuilder.baseUrl(PICSUM_BASE_URL)
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/{width}/{height}")
                            .queryParam("random", System.currentTimeMillis())
                            .build(width, height))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            if (imageBytes == null || imageBytes.length == 0) {
                throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to download image from picsum.photos");
            }

            log.info("Successfully downloaded image: {} bytes", imageBytes.length);

            return new Base64MultipartFile(imageBytes, "mock_generated_image.jpg", "image/jpeg", "image");

        } catch (Exception e) {
            log.error("Error downloading image from picsum.photos", e);
            throw new AppException(ErrorCode.GENERATION_ERROR, "Failed to download mock image: " + e.getMessage());
        }
    }
}
