package com.datn.datnbe.document.exam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a topic with its subtopics in the matrix generation request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic with subtopics for matrix generation")
public class TopicInput {

    @Schema(description = "Topic name (organizational container)", example = "Algebra", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Topic name is required")
    private String name;

    @Schema(description = "List of subtopic names under this topic", example = "[\"Linear Equations\", \"Quadratic Functions\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one subtopic is required")
    private List<String> subtopics;
}
