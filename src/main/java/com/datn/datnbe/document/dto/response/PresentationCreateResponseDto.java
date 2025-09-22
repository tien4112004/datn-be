package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("meta_data")
    private Object metaData;
    private List<SlideDto> slides;
}
