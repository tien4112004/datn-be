package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.ai.entity.BaseSlide;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PresentationResponse {
    List<BaseSlide> slides;
}
