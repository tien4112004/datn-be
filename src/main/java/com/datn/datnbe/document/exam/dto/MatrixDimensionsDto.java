package com.datn.datnbe.document.exam.dto;

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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixDimensionsDto {

    /**
     * List of topics (first dimension).
     */
    private List<DimensionTopicDto> topics;

    /**
     * List of difficulty levels (second dimension).
     */
    @Builder.Default
    private List<String> difficulties = Arrays.asList("easy", "medium", "hard");

    /**
     * List of question types (third dimension).
     */
    @JsonProperty("questionTypes")
    @JsonAlias("question_types")
    @Builder.Default
    private List<String> questionTypes = Arrays.asList("multiple_choice", "fill_in_blank", "true_false", "matching");
}
