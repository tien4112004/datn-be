package com.datn.datnbe.document.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a topic dimension in the exam matrix.
 * Topics serve as organizational containers for subtopics.
 * Note: Topics do not have IDs - subtopics are the primary dimension with IDs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionTopicDto {

    /**
     * Display name of the topic (e.g., "Algebra", "Geometry").
     * No ID needed - topics are organizational containers only.
     */
    private String name;

    /**
     * List of subtopics under this topic.
     * Subtopics have IDs and represent actual rows in the matrix.
     */
    private List<DimensionSubtopicDto> subtopics;
}
