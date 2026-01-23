package com.datn.datnbe.student.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a topic dimension in the exam matrix.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionTopicDto {

    /**
     * Unique identifier for the topic.
     */
    private String id;

    /**
     * Display name of the topic.
     */
    private String name;
}
