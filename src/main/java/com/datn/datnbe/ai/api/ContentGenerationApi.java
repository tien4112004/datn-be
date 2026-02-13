package com.datn.datnbe.ai.api;

import com.datn.datnbe.ai.dto.request.MindmapPromptRequest;
import com.datn.datnbe.ai.dto.request.OutlinePromptRequest;
import com.datn.datnbe.ai.dto.request.PresentationPromptRequest;
import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.datn.datnbe.document.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.document.dto.request.GenerateQuestionsFromTopicRequest;

import reactor.core.publisher.Flux;

public interface ContentGenerationApi {

    Flux<String> generateOutline(OutlinePromptRequest request, String traceId);

    Flux<String> generateSlides(PresentationPromptRequest request, String traceId);

    String generateOutlineBatch(OutlinePromptRequest request, String traceId);

    String generateSlidesBatch(PresentationPromptRequest request, String traceId);

    String generateMindmap(MindmapPromptRequest request, String traceId);

    /**
     * Generate an exam matrix using AI.
     * The matrix has dimensions: [topic][difficulty][question_type]
     * Each cell is in format "count:points".
     */
    AssignmentMatrixDto generateAssignmentMatrix(GenerateMatrixRequest request, String traceId);

    /**
     * Generate questions based on topic and requirements using AI
     */
    String generateQuestions(GenerateQuestionsFromTopicRequest request, String traceId);
}
