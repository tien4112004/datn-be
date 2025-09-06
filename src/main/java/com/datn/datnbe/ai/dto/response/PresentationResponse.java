package com.datn.datnbe.ai.dto.response;

import com.datn.datnbe.ai.entity.BaseSlide;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PresentationResponse {
    List<BaseSlide> slides;
}
