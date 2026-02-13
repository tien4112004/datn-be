package com.datn.datnbe.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a topic dimension in the exam matrix.
 * Topics are the first dimension in the matrix [topic][difficulty][question_type].
 * Topics may contain subtopics for organizational purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionTopicDto {

    /**
     * Numeric identifier for the topic (e.g., "1", "2", "3").
     * Based on the topic's position in the array.
     */
    private String id;

    /**
     * Display name of the topic (e.g., "Algebra", "Geometry").
     */
    private String name;

    /**
     * List of subtopics under this topic.
     * Subtopics are organizational subdivisions.
     * Questions from any subtopic count toward the parent topic's requirements.
     */
    private List<DimensionSubtopicDto> subtopics;

    /**
     * Flag indicating whether this topic uses context-based questions.
     * If true, the system will randomly select an appropriate context from the database
     * (matching grade/subject) and generate questions based on that context.
     * If false or null, questions are generated based on curriculum knowledge only.
     */
    @JsonProperty("hasContext")
    private Boolean hasContext;
}
