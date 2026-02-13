package com.datn.datnbe.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a subtopic within a topic dimension in the exam matrix.
 * Subtopics are organizational subdivisions of topics.
 * Questions from any subtopic count toward the parent topic's matrix cell requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionSubtopicDto {

    /**
     * Unique identifier for the subtopic.
     */
    private String id;

    /**
     * Display name of the subtopic.
     */
    private String name;
}
