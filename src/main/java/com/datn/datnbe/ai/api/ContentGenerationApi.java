package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.document.exam.dto.ExamMatrixDto;
import com.datn.datnbe.document.exam.dto.request.GenerateMatrixRequest;

import reactor.core.publisher.Flux;

public interface ContentGenerationApi {

    Flux<String> generateOutline(OutlinePromptRequest request);

    Flux<String> generateSlides(PresentationPromptRequest request);

    String generateOutlineBatch(OutlinePromptRequest request);

    String generateSlidesBatch(PresentationPromptRequest request);

    String generateMindmap(MindmapPromptRequest request);

    /**
     * Generate an exam matrix using AI.
     * The matrix has dimensions: [topic][difficulty][question_type]
     * Each cell is in format "count:points".
     */
    ExamMatrixDto generateExamMatrix(GenerateMatrixRequest request);
}
