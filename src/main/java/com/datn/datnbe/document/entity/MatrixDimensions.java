package com.datn.datnbe.document.entity;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixDimensions {
    private List<MatrixTopic> topics;
    private List<String> difficulties;    // lowercase: "knowledge", "comprehension", "application"
    private List<String> questionTypes;   // lowercase: "multiple_choice", "fill_in_blank", etc.
}
