package com.datn.datnbe.document.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentMatrix {
    private String grade;
    private String subject;
    private LocalDateTime createdAt;
    private MatrixDimensions dimensions;
    private List<List<List<String>>> matrix;  // 3D: [topic][difficulty][questionType] = "count:points"
    private Integer totalQuestions;
    private Double totalPoints;
}
