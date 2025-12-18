package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.request.SeatingLayoutRequest;
import com.datn.datnbe.cms.dto.response.SeatingLayoutResponseDto;

public interface SeatingLayoutApi {

    SeatingLayoutResponseDto getSeatingChart(String classId);

    SeatingLayoutResponseDto saveSeatingChart(String classId, SeatingLayoutRequest request);
}
