package com.datn.datnbe.document.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the dimensions of the exam matrix.
 * The matrix is indexed as: matrix[topic_index][difficulty_index][question_type_index]
 * Topics are the first dimension. Topics may contain subtopics for organizational purposes,
 * but questions from any subtopic count toward the parent topic's requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixDimensionsDto {

    /**
     * List of topics with their subtopics.
     * Topics are the first dimension in the matrix.
     * Subtopics are organizational subdivisions; questions from any subtopic
     * count toward the parent topic's cell requirements.
     */
    private List<DimensionTopicDto> topics;

    /**
     * List of difficulty levels (second dimension).
     */
    @Builder.Default
    private List<String> difficulties = Arrays.asList("KNOWLEDGE", "COMPREHENSION", "APPLICATION");

    /**
     * List of question types (third dimension).
     */
    @JsonProperty("questionTypes")
    @JsonAlias("question_types")
    @Builder.Default
    private List<String> questionTypes = Arrays.asList("MULTIPLE_CHOICE", "FILL_IN_BLANK", "OPEN_ENDED", "MATCHING");
}
