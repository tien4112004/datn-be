package com.datn.datnbe.student.exam.apiclient;

import com.datn.datnbe.ai.apiclient.AIApiClient;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.ExamMatrixV2Dto;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixV2Request;
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

    @Value("${ai.api.exam-matrix-v2-endpoint:/api/exams/generate-matrix/v2/mock}")
    private String examMatrixV2Endpoint;

    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Requesting matrix generation from AI worker via: {}", examMatrixEndpoint);
        return aiApiClient.post(examMatrixEndpoint, request, ExamMatrixDto.class);
    }

    /**
     * Generate a 3D exam matrix using the V2 endpoint.
     *
     * @param request The request with topics, difficulty, and question type requirements
     * @return The generated 3D exam matrix
     */
    public ExamMatrixV2Dto generateMatrixV2(GenerateMatrixV2Request request) {
        log.info("Requesting V2 matrix generation from AI worker via: {}", examMatrixV2Endpoint);
        return aiApiClient.post(examMatrixV2Endpoint, request, ExamMatrixV2Dto.class);
    }
}
