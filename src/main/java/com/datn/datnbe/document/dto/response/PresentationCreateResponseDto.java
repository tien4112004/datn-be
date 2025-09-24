package com.datn.datnbe.document.dto.response;

import java.util.List;
import java.util.Map;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationCreateResponseDto {
    private String id;
    private String title;
    @JsonAnyGetter
    private Map<String, Object> metaData;
    private List<SlideDto> slides;
}
