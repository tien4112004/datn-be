package com.datn.datnbe.document.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the exam matrix structure.
 *
 * Matrix is indexed as: matrix[topic_index][difficulty_index][question_type_index]
 * Each cell is "count:points" string format for that combination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamMatrixDto {

    /**
     * Matrix metadata (id, name, createdAt).
     */
    private MatrixMetadataDto metadata;

    /**
     * Matrix dimensions (topics, difficulties, questionTypes).
     */
    private MatrixDimensionsDto dimensions;

    /**
     * 3D matrix: [topic][difficulty][question_type] -> "count:points"
     * Each cell is a String in format "count:points".
     */
    private List<List<List<String>>> matrix;

    /**
     * Calculate total questions across all cells.
     */
    public int getTotalQuestions() {
        int total = 0;
        if (matrix != null) {
            for (List<List<String>> topicRow : matrix) {
                for (List<String> diffRow : topicRow) {
                    for (String cell : diffRow) {
                        if (cell != null && !cell.isEmpty()) {
                            String[] parts = cell.split(":");
                            total += Integer.parseInt(parts[0]); // count is first element
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
            for (List<List<String>> topicRow : matrix) {
                for (List<String> diffRow : topicRow) {
                    for (String cell : diffRow) {
                        if (cell != null && cell.contains(":")) {
                            String[] parts = cell.split(":");
                            total += Double.parseDouble(parts[1]); // points is second element
                        }
                    }
                }
            }
        }
        return total;
    }
}
