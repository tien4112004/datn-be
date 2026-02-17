package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.AssignmentMatrixDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Response DTO for matrix template operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatrixTemplateResponseDto {

    private String id;

    private String ownerId;

    private String name;

    private String subject;

    private String grade;

    private Date createdAt;

    private Date updatedAt;

    /**
     * Parsed matrix structure from matrixData JSON.
     */
    private AssignmentMatrixDto matrix;

    /**
     * Total number of questions in the matrix.
     */
    private Integer totalQuestions;

    /**
     * Total number of topics in the matrix.
     */
    private Integer totalTopics;
}
