package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.entity.MatrixDimensions;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatrixTemplateResponse {
    String id;
    String title;
    String grade;
    String subject;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    MatrixDimensions dimensions;
    List<List<List<String>>> matrix;
    Integer totalQuestions;
    Double totalPoints;
}
