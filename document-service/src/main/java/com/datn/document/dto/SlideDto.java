package com.datn.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class SlideDto {
    @NotBlank(message = "Slide ID cannot be blank")
    private String id;
    
    @Valid
    private List<SlideElementDto> elements;
    
    @Valid
    private SlideBackgroundDto background;
}