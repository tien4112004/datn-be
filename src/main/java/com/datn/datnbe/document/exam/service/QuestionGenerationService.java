package com.datn.datnbe.document.exam.service;

import com.datn.datnbe.ai.api.ContentGenerationApi;
import com.datn.datnbe.ai.dto.response.QuestionWithContextDto;
import com.datn.datnbe.document.exam.dto.request.GenerateQuestionsFromTopicRequest;
import com.datn.datnbe.document.exam.dto.response.GeneratedQuestionsResponse;
import com.datn.datnbe.document.entity.QuestionBankItem;
import com.datn.datnbe.document.entity.questiondata.*;
import com.datn.datnbe.document.exam.entity.QuestionContext;
import com.datn.datnbe.document.exam.enums.ContextType;
import com.datn.datnbe.document.exam.repository.QuestionContextRepository;
import com.datn.datnbe.document.exam.repository.ExamQuestionRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class QuestionGenerationService {

    ContentGenerationApi contentGenerationApi;
    ExamQuestionRepository questionRepository;
    QuestionContextRepository questionContextRepository;
    ObjectMapper objectMapper;

    @Transactional
    public GeneratedQuestionsResponse generateAndSaveQuestions(GenerateQuestionsFromTopicRequest request,
            UUID teacherId) {

        log.info("Generating questions for teacher: {}", teacherId);

        String jsonResult = contentGenerationApi.generateQuestions(request);

        List<QuestionWithContextDto> aiQuestions;
        try {
            aiQuestions = objectMapper.readValue(jsonResult, new TypeReference<List<QuestionWithContextDto>>() {
            });
            log.info("Parsed {} questions from AI response", aiQuestions.size());
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", jsonResult, e);
            throw new AppException(ErrorCode.AI_WORKER_SERVER_ERROR, "Failed to parse AI response");
        }

        Map<String, QuestionContext> contextCache = new HashMap<>();
        List<QuestionBankItem> questions = new ArrayList<>();

        for (QuestionWithContextDto qwc : aiQuestions) {
            log.info("Processing question: gradeLevel='{}', type={}",
                    qwc.getGradeLevel(),
                    qwc.getQuestion().getQuestionType());

            try {
                // Temporarily skip context handling
                QuestionBankItem question = convertToQuestionEntity(qwc,
                        teacherId.toString(),
                        request.getSubjectCode(),
                        null);

                // Validate required fields
                if (question.getType() == null) {
                    log.error("Question type is null for question: {}", qwc.getQuestion().getContent());
                    continue;
                }
                if (question.getDifficulty() == null) {
                    log.error("Question difficulty is null for question: {}", qwc.getQuestion().getContent());
                    continue;
                }
                if (question.getTitle() == null || question.getTitle().isEmpty()) {
                    log.error("Question title is null or empty");
                    continue;
                }
                if (question.getSubject() == null || question.getSubject().isEmpty()) {
                    log.error("Question subject is null or empty");
                    continue;
                }
                if (question.getData() == null) {
                    log.error("Question data is null for question: {}", qwc.getQuestion().getContent());
                    continue;
                }

                log.info("Valid question: type={}, difficulty={}, title={}, subject={}, grade={}",
                        question.getType(),
                        question.getDifficulty(),
                        question.getTitle().substring(0, Math.min(50, question.getTitle().length())),
                        question.getSubject(),
                        question.getGrade());

                questions.add(question);
            } catch (Exception e) {
                log.error("Failed to convert question: {}", qwc.getQuestion().getContent(), e);
            }
        }

        if (questions.isEmpty()) {
            log.warn("No valid questions to save");
            return GeneratedQuestionsResponse.builder()
                    .questionIds(List.of())
                    .totalGenerated(0)
                    .message("No valid questions generated")
                    .build();
        }

        log.info("Saving {} questions to database", questions.size());

        List<QuestionBankItem> savedQuestions;
        try {
            // Log each question before saving
            for (int i = 0; i < questions.size(); i++) {
                QuestionBankItem q = questions.get(i);
                log.info("Question {}: id={}, type={}, difficulty={}, grade={}, subject={}, data class={}",
                        i + 1,
                        q.getId(),
                        q.getType(),
                        q.getDifficulty(),
                        q.getGrade(),
                        q.getSubject(),
                        q.getData() != null ? q.getData().getClass().getSimpleName() : "null");
            }

            savedQuestions = questionRepository.saveAll(questions);
            log.info("Successfully saved {} questions", savedQuestions.size());
        } catch (Exception e) {
            log.error("Failed to save questions to database. Error type: {}, Message: {}",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);

            // Log the cause chain
            Throwable cause = e.getCause();
            int level = 1;
            while (cause != null && level < 5) {
                log.error("Cause level {}: {} - {}", level, cause.getClass().getSimpleName(), cause.getMessage());
                cause = cause.getCause();
                level++;
            }

            throw new AppException(ErrorCode.AI_RESULT_NOT_FOUND, "Failed to save questions: " + e.getMessage());
        }

        List<UUID> savedQuestionIds = savedQuestions.stream().map(q -> UUID.fromString(q.getId())).toList();

        return GeneratedQuestionsResponse.builder()
                .questionIds(savedQuestionIds)
                .totalGenerated(savedQuestionIds.size())
                .message("Successfully generated and saved questions")
                .build();
    }

    private QuestionContext createContextEntity(QuestionWithContextDto.QuestionContextDto contextDto, UUID ownerId) {
        ContextType contextType;
        try {
            contextType = ContextType.valueOf(contextDto.getContextType());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown context type: {}, defaulting to reading_passage", contextDto.getContextType());
            contextType = ContextType.reading_passage;
        }

        return QuestionContext.builder()
                .ownerId(ownerId)
                .contextType(contextType)
                .title(contextDto.getTitle())
                .content(contextDto.getContent())
                .metadata(contextDto.getMetadata() != null ? contextDto.getMetadata() : new HashMap<>())
                .build();
    }

    private QuestionBankItem convertToQuestionEntity(QuestionWithContextDto qwc,
            String ownerId,
            String subjectCode,
            String contextId) {

        // Validate inputs
        if (qwc == null || qwc.getQuestion() == null) {
            throw new IllegalArgumentException("Question data is null");
        }
        if (subjectCode == null || subjectCode.isEmpty()) {
            throw new IllegalArgumentException("Subject code is required");
        }

        QuestionType type = parseQuestionType(qwc.getQuestion().getQuestionType());
        Difficulty difficulty = parseDifficulty(qwc.getDifficulty());
        String title = qwc.getQuestion().getContent();

        if (title == null || title.isEmpty()) {
            title = "Untitled Question";
            log.warn("Question has no content, using default title");
        }

        Object data = buildQuestionData(qwc.getQuestion());

        String questionId = UUID.randomUUID().toString();
        log.debug("Creating QuestionBankItem: id={}, type={}, difficulty={}, grade={}, chapter={}, subject={}",
                questionId,
                type,
                difficulty,
                qwc.getGradeLevel(),
                qwc.getTopic(),
                subjectCode);

        return QuestionBankItem.builder()
                .id(questionId)
                .type(type)
                .difficulty(difficulty)
                .title(title)
                .explanation(qwc.getQuestion().getExplanation())
                .grade(qwc.getGradeLevel())
                .chapter(qwc.getTopic())
                .subject(subjectCode)
                .contextId(contextId)
                .data(data)
                .ownerId(ownerId)
                .build();
    }

    private Object buildQuestionData(QuestionWithContextDto.QuestionDto question) {
        String questionType = question.getQuestionType().toUpperCase();
        try {
            return switch (questionType) {
                case "MULTIPLE_CHOICE" -> buildMultipleChoiceData(question);
                case "FILL_BLANK", "FILL_IN_BLANK" -> buildFillInBlankData(question);
                case "MATCHING" -> buildMatchingData(question);
                case "LONG_ANSWER", "OPEN_ENDED" -> buildOpenEndedData(question);
                case "TRUE_FALSE" -> buildTrueFalseData(question);
                default -> {
                    log.warn("Unknown question type: {}, defaulting to multiple choice", questionType);
                    yield buildMultipleChoiceData(question);
                }
            };
        } catch (Exception e) {
            log.error("Error building question data for type: {}", questionType, e);
            // Return a safe default instead of the DTO
            return MultipleChoiceData.builder().options(List.of()).build();
        }
    }

    private MultipleChoiceData buildMultipleChoiceData(QuestionWithContextDto.QuestionDto question) {
        List<MultipleChoiceOption> options = new ArrayList<>();
        if (question.getAnswers() instanceof List) {
            List<?> answerList = (List<?>) question.getAnswers();
            String correctAnswer = question.getCorrectAnswer() != null ? question.getCorrectAnswer().toString() : null;

            for (Object opt : answerList) {
                String optionText = opt.toString();
                options.add(MultipleChoiceOption.builder()
                        .text(optionText)
                        .isCorrect(optionText.equals(correctAnswer))
                        .build());
            }
        }
        return MultipleChoiceData.builder().options(options).build();
    }

    private FillInBlankData buildFillInBlankData(QuestionWithContextDto.QuestionDto question) {
        List<BlankSegment> segments = new ArrayList<>();
        List<String> answers = new ArrayList<>();

        if (question.getAnswers() instanceof List) {
            ((List<?>) question.getAnswers()).forEach(ans -> answers.add(ans.toString()));
        } else if (question.getCorrectAnswer() != null) {
            answers.add(question.getCorrectAnswer().toString());
        }

        // Create a single blank segment with the question content and acceptable answers
        segments.add(BlankSegment.builder()
                .type(BlankSegment.SegmentType.BLANK)
                .content("")
                .acceptableAnswers(answers)
                .build());

        return FillInBlankData.builder().segments(segments).build();
    }

    private MatchingData buildMatchingData(QuestionWithContextDto.QuestionDto question) {
        List<MatchingPair> pairs = new ArrayList<>();

        if (question.getAnswers() == null) {
            log.warn("Matching question has null answers");
            return MatchingData.builder().pairs(pairs).build();
        }

        try {
            if (question.getAnswers() instanceof Map) {
                ((Map<?, ?>) question.getAnswers()).forEach((k,
                        v) -> pairs.add(MatchingPair.builder()
                                .left(k != null ? k.toString() : "")
                                .right(v != null ? v.toString() : "")
                                .build()));
            } else if (question.getAnswers() instanceof List) {
                List<?> list = (List<?>) question.getAnswers();
                for (int i = 0; i < list.size(); i += 2) {
                    if (i + 1 < list.size()) {
                        pairs.add(MatchingPair.builder()
                                .left(list.get(i) != null ? list.get(i).toString() : "")
                                .right(list.get(i + 1) != null ? list.get(i + 1).toString() : "")
                                .build());
                    }
                }
            } else {
                log.warn("Matching question answers is neither Map nor List: {}", question.getAnswers().getClass());
            }
        } catch (Exception e) {
            log.error("Error parsing matching data", e);
        }

        return MatchingData.builder().pairs(pairs).build();
    }

    private OpenEndedData buildOpenEndedData(QuestionWithContextDto.QuestionDto question) {
        return OpenEndedData.builder()
                .expectedAnswer(question.getCorrectAnswer() != null ? question.getCorrectAnswer().toString() : null)
                .build();
    }

    private MultipleChoiceData buildTrueFalseData(QuestionWithContextDto.QuestionDto question) {
        String correctAnswer = question.getCorrectAnswer() != null ? question.getCorrectAnswer().toString() : null;

        List<MultipleChoiceOption> options = Arrays.asList(
                MultipleChoiceOption.builder().text("Đúng").isCorrect("Đúng".equals(correctAnswer)).build(),
                MultipleChoiceOption.builder().text("Sai").isCorrect("Sai".equals(correctAnswer)).build());

        return MultipleChoiceData.builder().options(options).build();
    }

    private QuestionType parseQuestionType(String questionType) {
        try {
            String normalized = questionType.toUpperCase();
            return switch (normalized) {
                case "FILL_BLANK", "FILL_IN_BLANK" -> QuestionType.FILL_IN_BLANK;
                case "LONG_ANSWER" -> QuestionType.OPEN_ENDED;
                case "TRUE_FALSE" -> QuestionType.MULTIPLE_CHOICE;
                default -> QuestionType.valueOf(normalized);
            };
        } catch (IllegalArgumentException e) {
            log.warn("Unknown question type: {}, defaulting to MULTIPLE_CHOICE", questionType);
            return QuestionType.MULTIPLE_CHOICE;
        }
    }

    private Difficulty parseDifficulty(String difficulty) {
        String normalized = difficulty != null ? difficulty.toLowerCase() : "easy";
        return switch (normalized) {
            case "easy" -> Difficulty.KNOWLEDGE;
            case "medium" -> Difficulty.COMPREHENSION;
            case "hard" -> Difficulty.APPLICATION;
            default -> Difficulty.KNOWLEDGE;
        };
    }
}
