package com.datn.datnbe.student.exam.service;

import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.apiclient.ExamGenerationApiClient;
import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamService implements ExamApi {

    ExamGenerationApiClient examGenerationApiClient;

    @Override
    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Generating exam matrix for topic: {}", request.getTopic());
        return examGenerationApiClient.generateMatrix(request);
    }
}

