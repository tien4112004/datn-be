package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.entity.MatrixDimensions;
import lombok.Data;

import java.util.List;

@Data
public class MatrixTemplateUpdateRequest {
    private String title;
    private String grade;
    private String subject;
    private MatrixDimensions dimensions;
    private List<List<List<String>>> matrix;
    private Integer totalQuestions;
    private Double totalPoints;
}
