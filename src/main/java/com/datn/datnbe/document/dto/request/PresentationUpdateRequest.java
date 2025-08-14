package com.datn.datnbe.document.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.datn.datnbe.document.dto.SlideDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationUpdateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @NotNull(message = "Title cannot be null")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Slides cannot be null")
    @NotEmpty(message = "Presentation must contain at least one slide")
    @Valid
    private List<SlideDto> slides;
}
