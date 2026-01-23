package com.datn.datnbe.student.exam.dto;

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
     * ISO timestamp of creation.
     */
    @JsonProperty("createdAt")
    @JsonAlias("created_at")
    private String createdAt;
}
