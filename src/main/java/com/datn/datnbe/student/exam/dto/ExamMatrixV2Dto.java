package com.datn.datnbe.student.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the V2 3D exam matrix structure.
 *
 * Matrix is indexed as: matrix[topic_index][difficulty_index][question_type_index]
 * Each cell is [count, points] array for that combination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamMatrixV2Dto {

    /**
     * Matrix metadata (id, name, createdAt).
     */
    private MatrixMetadataDto metadata;

    /**
     * Matrix dimensions (topics, difficulties, questionTypes).
     */
    private MatrixDimensionsDto dimensions;

    /**
     * 3D matrix: [topic][difficulty][question_type] -> [count, points]
     * Each cell is a List containing [count (Number), points (Number)].
     */
    private List<List<List<List<Number>>>> matrix;

    /**
     * Calculate total questions across all cells.
     */
    public int getTotalQuestions() {
        int total = 0;
        if (matrix != null) {
            for (List<List<List<Number>>> topicRow : matrix) {
                for (List<List<Number>> diffRow : topicRow) {
                    for (List<Number> cell : diffRow) {
                        if (cell != null && !cell.isEmpty()) {
                            total += cell.get(0).intValue(); // count is first element
                        }
                    }
                }
            }
        }
        return total;
    }

    /**
     * Calculate total points across all cells.
     */
    public double getTotalPoints() {
        double total = 0.0;
        if (matrix != null) {
            for (List<List<List<Number>>> topicRow : matrix) {
                for (List<List<Number>> diffRow : topicRow) {
                    for (List<Number> cell : diffRow) {
                        if (cell != null && cell.size() > 1) {
                            total += cell.get(1).doubleValue(); // points is second element
                        }
                    }
                }
            }
        }
        return total;
    }
}
