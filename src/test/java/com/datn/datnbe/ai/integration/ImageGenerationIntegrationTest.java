//package com.datn.datnbe.ai.integration;
//
//import com.datn.datnbe.ai.api.ContentGenerationApi;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
///**
// * Integration test for Image Generation API
// * Note: This test requires proper Google Cloud credentials and environment setup
// */
//@SpringBootTest
//@ActiveProfiles("test")
//public class ImageGenerationIntegrationTest {
//
//    @Autowired
//    private ContentGenerationApi contentGenerationApi;
//
//    @Test
//    public void testImageGenerationRequest() {
//        // This test is disabled by default as it requires proper GCP setup
//        // Uncomment and configure your environment to run this test
//
//        /*
//        ImageGenerationRequest request = ImageGenerationRequest.builder()
//                .prompt("A beautiful sunset over a mountain landscape")
//                .aspectRatio("16:9")
//                .sampleCount(1)
//                .safetyFilterLevel("block_some")
//                .personGeneration("allow_adult")
//                .build();
//
//        StepVerifier.create(contentGenerationApi.generateImage(request))
//                .expectNextMatches(response -> {
//                    return response.getImageBase64() != null &&
//                           !response.getImageBase64().isEmpty() &&
//                           response.getMimeType().equals("image/png") &&
//                           response.getPrompt().equals(request.getPrompt());
//                })
//                .verifyComplete();
//        */
//    }
//}
