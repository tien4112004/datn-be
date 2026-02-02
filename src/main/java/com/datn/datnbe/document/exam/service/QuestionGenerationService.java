package com.datn.datnbe.document.exam.service;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.document.dto.response.QuestionResponseDto;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.exam.dto.request.GenerateQuestionsFromTopicRequest;
import com.datn.datnbe.document.exam.dto.response.GeneratedQuestionsResponse;
import com.datn.datnbe.document.mapper.QuestionEntityMapper;
import com.datn.datnbe.document.repository.QuestionRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class QuestionGenerationService {

    ContentGenerationApi contentGenerationApi;
    QuestionRepository questionRepository;
    QuestionEntityMapper questionMapper;
    ObjectMapper objectMapper;

    @Transactional
    public GeneratedQuestionsResponse generateAndSaveQuestions(GenerateQuestionsFromTopicRequest request,
            String teacherId) {

        log.info("Generating questions for teacher: {}", teacherId);

        // Call AI service to generate questions (returns JSON string)
        String jsonResult = contentGenerationApi.generateQuestions(request);
        log.info("AI response received, length: {} chars", jsonResult != null ? jsonResult.length() : 0);
        log.debug("AI response JSON: {}", jsonResult);

        // Parse JSON string to list of Question POJOs
        List<Question> aiQuestions;
        try {
            aiQuestions = objectMapper.readValue(jsonResult, new TypeReference<List<Question>>() {
            });
            log.info("Parsed {} questions from AI response", aiQuestions.size());
        } catch (Exception e) {
            log.error("Failed to parse AI response. Error: {}, Response preview: {}",
                    e.getMessage(),
                    jsonResult != null && jsonResult.length() > 500
                            ? jsonResult.substring(0, 500) + "..."
                            : jsonResult);
            log.debug("Full AI response that failed to parse: {}", jsonResult);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }

        // Validate questions with detailed error messages
        List<String> validationErrors = new java.util.ArrayList<>();
        for (int i = 0; i < aiQuestions.size(); i++) {
            Question q = aiQuestions.get(i);
            int questionNum = i + 1;

            // Validate required fields
            if (q.getType() == null)
                validationErrors.add("Question " + questionNum + ": missing type");
            if (q.getDifficulty() == null)
                validationErrors.add("Question " + questionNum + ": missing difficulty");
            if (q.getTitle() == null || q.getTitle().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty title");
            if (q.getSubject() == null || q.getSubject().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty subject");
            if (q.getGrade() == null || q.getGrade().trim().isEmpty())
                validationErrors.add("Question " + questionNum + ": missing or empty grade");
            if (q.getData() == null)
                validationErrors.add("Question " + questionNum + ": missing data");

            if (validationErrors.isEmpty()) {
                log.info("Valid question {}: type={}, difficulty={}, title={}, subject={}, grade={}",
                        questionNum,
                        q.getType(),
                        q.getDifficulty(),
                        q.getTitle(),
                        q.getSubject(),
                        q.getGrade());
            }
        }

        if (!validationErrors.isEmpty()) {
            String errorMessage = "Question validation failed:\n" + String.join("\n", validationErrors);
            log.error(errorMessage);
            throw new AppException(ErrorCode.INVALID_ELEMENT_DATA, errorMessage);
        }

        // Convert to QuestionBankItem entities (without IDs - let JPA generate them)
        List<QuestionBankItem> questionEntities = aiQuestions.stream()
                .map(q -> convertToQuestionBankItem(q, teacherId))
                .collect(Collectors.toList());

        log.info("Saving {} questions to database", questionEntities.size());

        // Batch save all questions
        List<QuestionBankItem> savedQuestions;
        try {
            savedQuestions = questionRepository.saveAll(questionEntities);
            log.info("Successfully saved {} questions", savedQuestions.size());
        } catch (Exception e) {
            log.error("Failed to save questions to database. Error type: {}, Message: {}",
                    e.getClass().getSimpleName(),
                    e.getMessage());
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR,
                    "Failed to save questions to database: " + e.getMessage());
        }

        // Extract IDs and full question details
        List<QuestionResponseDto> questionDetails = savedQuestions.stream()
                .map(questionMapper::toResponseDto)
                .collect(Collectors.toList());

        return GeneratedQuestionsResponse.builder()
                .totalGenerated(savedQuestions.size())
                .questions(questionDetails)
                .build();
    }

    private QuestionBankItem convertToQuestionBankItem(Question aiQuestion, String ownerId) {
        // Use topic name directly as chapter field (String)
        // The chapter field stores the topic name for filtering by grade, subject, and topic
        String topicName = aiQuestion.getChapter();

        if (topicName != null && !topicName.isEmpty()) {
            log.debug("Setting topic name (chapter): '{}', subject: '{}', grade: '{}'",
                    topicName,
                    aiQuestion.getSubject(),
                    aiQuestion.getGrade());
        }

        return QuestionBankItem.builder()
                // DO NOT set id - let JPA generate it
                .type(aiQuestion.getType())
                .difficulty(aiQuestion.getDifficulty())
                .title(aiQuestion.getTitle())
                .titleImageUrl(aiQuestion.getTitleImageUrl())
                .explanation(aiQuestion.getExplanation())
                .grade(aiQuestion.getGrade())
                .chapter(topicName) // Store topic name directly as String
                .subject(aiQuestion.getSubject())
                .data(aiQuestion.getData())
                .ownerId(ownerId)
                // createdAt and updatedAt are auto-generated
                .build();
    }
}
