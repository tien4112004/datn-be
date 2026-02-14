package com.datn.datnbe.document.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata for the exam matrix.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixMetadataDto {

    /**
     * Unique identifier for the matrix.
     */
    private String id;

    /**
     * Name of the matrix.
     */
    private String name;

    /**
     * Grade level for the matrix (e.g., "1", "2", "3", "4", "5").
     * Used for filtering questions from the question bank.
     */
    private String grade;

    /**
     * Subject for the matrix (T, TV, TA).
     * Used for filtering questions from the question bank.
     */
    @JsonProperty("subject")
    @JsonAlias("subject")
    private String subject;

    /**
     * ISO timestamp of creation.
     */
    @JsonProperty("createdAt")
    @JsonAlias("created_at")
    private String createdAt;
}
