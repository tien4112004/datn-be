package com.datn.document.dto.response;

import com.datn.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationCreateResponseDto {
    private String title;
    private List<SlideDto> presentation;
}