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
 * The matrix is indexed as: matrix[subtopic_index][difficulty_index][question_type_index]
 * Topics serve as organizational groupings, while subtopics are the actual first dimension.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixDimensionsDto {

    /**
     * List of topics with their subtopics.
     * Topics are organizational containers; subtopics are the actual first dimension in the matrix.
     * Matrix rows correspond to flattened subtopics across all topics.
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
