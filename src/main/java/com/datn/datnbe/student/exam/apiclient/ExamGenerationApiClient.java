package com.datn.datnbe.student.exam.apiclient;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateQuestionsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamGenerationApiClient {

    private final AIApiClient aiApiClient;

    public Flux<String> generateMatrix(GenerateMatrixRequest request) {
        log.info("Requesting matrix generation from AI worker");
        return aiApiClient.postSse("/exams/generate-matrix", request);
    }

    public Flux<String> generateQuestions(GenerateQuestionsRequest request) {
        log.info("Requesting question generation from AI worker");
        return aiApiClient.postSse("/exams/generate-questions", request);
    }
}
