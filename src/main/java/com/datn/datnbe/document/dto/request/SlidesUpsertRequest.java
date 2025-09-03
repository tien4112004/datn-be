package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.SlideDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlidesUpsertRequest {
    public List<SlideDto> slides;
}
