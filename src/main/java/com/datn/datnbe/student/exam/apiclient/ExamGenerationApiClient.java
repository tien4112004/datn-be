package com.datn.datnbe.student.exam.apiclient;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateQuestionsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamGenerationApiClient {

    private final AIApiClient aiApiClient;

    @Value("${ai.api.exam-matrix-endpoint}")
    private String examMatrixEndpoint;

    @Value("${ai.api.exam-questions-endpoint}")
    private String examQuestionsEndpoint;

    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Requesting matrix generation from AI worker via: {}", examMatrixEndpoint);
        return aiApiClient.post(examMatrixEndpoint, request, ExamMatrixDto.class);
    }

    public String generateQuestions(GenerateQuestionsRequest request) {
        log.info("Requesting question generation from AI worker via: {}", examQuestionsEndpoint);
        return aiApiClient.post(examQuestionsEndpoint, request, String.class);
    }
}
