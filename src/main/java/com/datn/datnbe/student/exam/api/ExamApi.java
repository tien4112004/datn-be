package com.datn.datnbe.student.exam.api;

import com.datn.datnbe.student.exam.dto.ExamMatrixDto;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;

public interface ExamApi {

    ExamMatrixDto generateMatrix(GenerateMatrixRequest request);
}
