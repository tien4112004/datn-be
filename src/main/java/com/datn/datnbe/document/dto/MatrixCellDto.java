package com.datn.datnbe.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a cell in the 3D exam matrix.
 * Each cell contains the count of questions and points for a specific
 * combination of topic, difficulty, and question type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixCellDto {

    /**
     * Number of questions required for this cell.
     */
    @Builder.Default
    private Integer count = 0;

    /**
     * Total points allocated for this cell.
     */
    @Builder.Default
    private Double points = 0.0;
}
