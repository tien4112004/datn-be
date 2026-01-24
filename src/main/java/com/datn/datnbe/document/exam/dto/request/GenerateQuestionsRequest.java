package com.datn.datnbe.document.exam.dto.request;

import com.datn.datnbe.document.exam.dto.MatrixItemDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsRequest {

    @NotNull(message = "Matrix is required")
    private List<MatrixItemDto> matrix;
}
