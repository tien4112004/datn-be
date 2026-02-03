package com.datn.datnbe.document.exam.dto.request;

import com.datn.datnbe.document.entity.questiondata.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsFromTopicRequest {

    @NotBlank(message = "Grade is required")
    private String grade;

    @NotEmpty(message = "Topic is required")
    private String topic;

    @NotEmpty(message = "Subject is required")
    private String subject; // T, TV, TA

    @NotNull(message = "Question counts per difficulty are required")
    private Map<String, Integer> questionsPerDifficulty;
    // e.g., { "easy": 5, "medium": 3, "hard": 2 }
    // Keys should be valid ExamDifficulty enum names (case-insensitive)

    @NotEmpty(message = "Question types are required")
    private List<QuestionType> questionTypes;
    // e.g., ["multiple_choice", "fill_blank"]

    private String additionalRequirements; // Optional

    private String provider; // Optional: AI provider (e.g., "google", "openai"). Default: "google"

    private String model; // Optional: AI model (e.g., "gemini-2.5-flash-lite", "gpt-4"). Default: "gemini-2.5-flash-lite"
}
