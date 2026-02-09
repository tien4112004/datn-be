package com.datn.datnbe.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a topic dimension in the exam matrix.
 * Topics serve as organizational containers for subtopics.
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
     * Subtopics have IDs and represent actual rows in the matrix.
     */
    private List<DimensionSubtopicDto> subtopics;
}
