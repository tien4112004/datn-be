package com.datn.document.dto.request;

import com.datn.document.dto.SlideDto;
import jakarta.validation.Valid;
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
public class PresentationUpdateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Valid
    private List<SlideDto> slides;
}
