package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.MatrixTemplateCreateRequest;
import com.datn.datnbe.document.dto.request.MatrixTemplateUpdateRequest;
import com.datn.datnbe.document.dto.response.MatrixTemplateResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface MatrixTemplateApi {

    MatrixTemplateResponse createMatrixTemplate(MatrixTemplateCreateRequest request);

    PaginatedResponseDto<MatrixTemplateResponse> getMatrixTemplates(int page, int size);

    MatrixTemplateResponse getMatrixTemplateById(String id);

    MatrixTemplateResponse updateMatrixTemplate(String id, MatrixTemplateUpdateRequest request);

    void deleteMatrixTemplate(String id);
}
