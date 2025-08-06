package com.datn.aiservice.dto.response;

import java.util.List;

import com.datn.aiservice.entity.BaseSlide;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PresentationResponse {
    List<BaseSlide> slides;
}
