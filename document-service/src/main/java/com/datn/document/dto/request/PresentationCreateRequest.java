package com.datn.document.dto.request;

import com.datn.document.dto.SlideDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationCreateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title = null;

    @NotNull(message = "Slides cannot be null")
    @NotEmpty(message = "Presentation must contain at least one slide")
    @Valid
    private List<SlideDto> slides;
}
